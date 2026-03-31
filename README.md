# Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements

- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator

## API Documentation

For documentation openAPI is included, and it can be found under the following url: **[http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html)**

**Feel free to change the structure of the solution, use a different library etc.**

## My Implementation

When I was building this solution, my goal was to make it as close to production-ready as possible within the scope of the challenge. I focused not only on functional correctness, but also on operational concerns such as observability, quality gates, and safe handling of sensitive payment data.

This project is a Spring Boot payment gateway built to satisfy the Checkout.com "Building a payment gateway" challenge.

### Core Assumptions

While implementing this challenge, I made the following core assumptions to keep the solution as production-ready as possible:

- **Observability and monitoring are first-class concerns.**
  I implemented structured logging with meaningful log levels and added API monitoring. Monitoring data is routed through Kafka/ELK and visualized in Grafana.
- **Comprehensive test coverage should be enforced in CI/build.**
  JaCoCo is configured and coverage verification is enforced through the Gradle build lifecycle (`check` depends on `jacocoTestCoverageVerification`).
- **Sensitive card data must not be retained.**
  The full PAN and CVV are not persisted in stored payment records. Only the last four digits of the card are stored and returned.

### Architecture (high level)

The payment flow is implemented across:

- `PaymentGatewayController` (HTTP API)
- `PaymentGatewayService` (orchestration)
- `PaymentRequest` / `Payment` (validation + domain state)
- `BankService` (HTTP call to the bank simulator)
- `PaymentsRepository` (in-memory storage for retrieval)

### API Endpoints

The app currently exposes:

- `POST /payment` to process a payment
- `GET /payment/{id}` to retrieve a previously made payment

Note: the assessment mentions `/payments` (plural). If strict endpoint matching is required, you may need to rename these paths.

OpenAPI docs:
`http://localhost:8090/swagger-ui/index.html`

### Request & Response

Request (`POST /payment`) expects:
`card_number`, `expiry_month`, `expiry_year`, `currency`, `amount`, `cvv` (using the snake_case JSON field names from the assessment).

Success response includes:
`id`, `status` (`Authorized` or `Declined`), `card_number_last_four`, `expiry_month`, `expiry_year`, `currency`, `amount`.

Only the last four digits of the card are stored/returned (no CVV and no full PAN).

### Validation and "Rejected"

Gateway validation occurs before calling the bank simulator:

- card number: digits only and length `14-19`
- CVV: `3-4` digits
- expiry date: must be in the future
- currency: only `USD`, `EUR`, `GBP`
- amount: must be greater than `0`

If validation fails, the gateway returns `400 Bad Request` with an `ErrorResponse` payload (instead of returning `PaymentResponseDTO.status="Rejected"`).

### Bank Simulator Integration

`BankService` sends an HTTP POST to the simulator using:
`bank.uri=http://bank_simulator:8080/payments`

`PaymentRequestDTO` includes `expiry_date` derived from `expiry_month` + `expiry_year` via the `getExpiryDate()` method.

### Error Handling

`GlobalExceptionHandler` maps:

- invalid gateway request -> `400 Bad Request`
- bank unavailable/error -> `503 Service Unavailable`
- unknown payment id -> `404 Not Found`

### API Monitoring (Kafka)

`ApiMonitoringAspect` records controller method calls and sends sanitized events to Kafka topic `api-monitoring`.
Sensitive payment fields (card number + CVV) are excluded.

Monitoring stack in `docker-compose.yml` includes:

- Kafka + Kafka Connect for event transport
- Logstash + Elasticsearch for processing/storage
- Grafana for dashboards/visualization

### Running Locally

The recommended way to run is:
`docker compose up`

This starts the bank simulator, payment gateway, and supporting observability services (Kafka, Kafka Connect, Logstash, Elasticsearch, Grafana).