# Movie Booking Service

A Spring Boot REST API for browsing movie shows and booking tickets. Built as a focused backend exercise covering key distributed systems concerns: optimistic concurrency, discounting rules, and JWT-based auth.

---

## Tech Stack

### Languages

| Purpose | Choice | Rationale |
|---|---|---|
| Core service | **Java 17** | LTS, virtual threads (Project Loom) for high-concurrency booking paths in Java 21+; strong ecosystem for enterprise Spring |
| Database migrations | **SQL** (Flyway scripts) | Plain SQL keeps migrations readable and version-controlled without ORM coupling |
| Build / scripting | **Bash / YAML** | CI pipeline steps, Docker entrypoints |
| AI/ML pipelines | **Python** | Data science ecosystem (Pandas, scikit-learn, PyTorch) for recommendation and fraud models; exposed as internal REST microservice called from Java |

### Frameworks & Libraries

| Layer | Technology | Rationale |
|---|---|---|
| Web / REST | Spring Boot 3.2.4 | Autoconfiguration reduces boilerplate; production-ready actuator, metrics |
| Persistence | Spring Data JPA + Hibernate | Parameterized queries prevent SQL injection; PESSIMISTIC_WRITE lock API for concurrency |
| Security | Spring Security + JWT (jjwt 0.11.5) | Stateless auth scales horizontally; role-based access built-in |
| API Docs | SpringDoc OpenAPI 2.3 / Swagger UI | Contract-first documentation; importable into Postman |
| Resilience | Resilience4j | Circuit breaker + retry for payment gateway calls; bulkhead for theatre API integrations |
| Build | Maven 3.9 | Deterministic dependency resolution; Spring BOM manages CVE-patched transitive versions |

### AI / Machine Learning

| Use Case | Approach |
|---|---|
| **Personalised recommendations** | Collaborative filtering on booking history → "Shows you may like" on the browse screen. Model trained weekly on anonymised data, served via a lightweight Python FastAPI microservice |
| **Dynamic pricing** | Demand-based price suggestions to theatre admins — gradient boosting model (XGBoost) trained on historical occupancy, day-of-week, and lead-time features |
| **Payment fraud detection** | Real-time anomaly scoring on payment requests (unusual amount, new device, rapid retries) using an isolation forest model; score returned alongside gateway response |
| **Chatbot / support** | LLM-powered booking assistant for natural language queries: "Show me action movies in Pune this weekend under ₹300" → translates to structured API calls via any major LLM API |

> **Current implementation:** AI layer is not included in this codebase — the service is designed with clean interfaces (`PaymentGatewayPort`, `DiscountStrategy`) so these models slot in without touching the core domain.

### Database

| Component | Choice | Rationale |
|---|---|---|
| Primary store | **PostgreSQL 16** | ACID transactions, row-level locking (PESSIMISTIC_WRITE), JSONB for flexible theatre metadata |
| Caching | **Redis** | Seat availability cache (TTL 30s); distributed seat reservation lock (TTL 10 min for the "select→pay" window) |
| Search | **Elasticsearch** (future) | Full-text movie search across titles, cast, descriptions once catalogue grows |
| Analytics | **Redshift / BigQuery** | OLAP queries on booking trends, city-level demand — separated from OLTP to avoid query contention |

### Integration & Data Technologies

| Technology | Role |
|---|---|
| **Apache Kafka** | Async event bus: `BookingConfirmedEvent` → email service; `BookingCancelledEvent` → refund service; `ShowCancelledEvent` → notify affected customers. TODOs in `BookingServiceImpl` and `AdminController` mark the publish points |
| **Webhook ingest** | Theatres with existing IT push show/inventory updates via signed webhooks to `/api/webhooks/shows`; HMAC-SHA256 signature verification prevents spoofing |
| **REST adapters** | `TheatreIntegrationPort` interface with per-vendor implementations (PVR REST API, INOX legacy SFTP batch, new cloud-native theatres via our own admin APIs) |
| **Payment webhooks** | Razorpay / Stripe POST payment status to `/api/webhooks/payment`; verified via gateway signature before updating `Payment.status` |
| **SendGrid / AWS SES** | Transactional email for booking confirmation, cancellation, and refund receipts — triggered by Kafka consumers |
| **OpenTelemetry** | Distributed tracing with `bookingId` as trace ID, exported to Jaeger; spans across booking → payment → notification |

### Cloud Technologies (AWS)

| Service | Purpose |
|---|---|
| **ECS Fargate** | Containerised service deployment — stateless pods scale horizontally behind ALB |
| **RDS PostgreSQL Multi-AZ** | Managed primary + standby; automatic failover ~60s; read replicas for `@Transactional(readOnly=true)` queries |
| **ElastiCache (Redis)** | Managed Redis cluster for seat cache and distributed locks |
| **ALB** | Layer-7 load balancing; health checks; blue-green deployment via target group swap |
| **CloudFront (CDN)** | Edge-cache `GET /api/shows` and `GET /api/theatres` responses; reduces DB read load by 60–70% for browse traffic |
| **MSK (Managed Kafka)** | Kafka clusters without ops overhead; cross-AZ replication built-in |
| **Secrets Manager** | JWT secret, DB credentials, payment gateway API keys — TODO in `JwtTokenProvider` marks the integration point |
| **WAF** | Rate limiting (100 req/min per IP), geo-blocking, OWASP managed rule sets in front of ALB |
| **CloudWatch + X-Ray** | Metrics, logs, and distributed traces; alarms on booking failure rate and payment latency |

> **Current implementation:** App runs locally against a PostgreSQL instance. All AWS components are referenced in TODOs and architecture decisions — designed to be cloud-ready (stateless, externalised config, containerisable via `Dockerfile`).

### Preferred Editor

**Visual Studio Code** — used to build and present this solution.
- Spring Boot Extension Pack for run/debug configuration
- REST Client extension (`.http` files) for endpoint testing alongside Swagger UI
- SQLTools extension for live PostgreSQL inspection during demo
- GitLens for commit history and code authorship

---

## Architecture Decision: Why a Single Service

For a focused exercise scope, splitting into microservices (separate Booking, Inventory, Payment services) would be premature — the coordination overhead (Kafka, saga patterns, distributed tracing) would outweigh the benefit at this scale. If this were a production system handling 100K+ daily bookings, I'd extract a separate Seat Inventory service and use Kafka choreography to decouple the booking confirmation flow from inventory updates. The comment in `BookingServiceImpl` marks exactly where that event publish would go.

---

## Running Locally

### Option A — Docker (recommended, no Java/Maven needed)

**Prerequisite:** Docker Desktop installed and running.

```bash
git clone https://github.com/amishasinha35/movie-booking-ps.git
cd movie-booking-ps
docker-compose up --build
```

That's it. Docker builds the JAR, starts PostgreSQL, waits for it to be healthy, then starts the app.

- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

> On first run `ddl-auto=create` creates the schema and `data.sql` seeds 2 theatres, 4 shows, and 20 seats per show.

To stop: `docker-compose down`

---

### Option B — Local (Java + Maven required)

**Prerequisites:** Java 17+, Maven 3.9+, PostgreSQL 14+ running locally.

```bash
# Create the database (once)
psql -U postgres -c "CREATE DATABASE moviebookingdb;"

# Run the app
mvn spring-boot:run
```

---

## API Reference

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "password"}'
```

Response:
```json
{ "token": "<jwt>", "username": "customer1", "role": "ROLE_CUSTOMER" }
```

Save the token: `export TOKEN=<jwt>`

**Available users:** `customer1 / password` (ROLE_CUSTOMER), `admin / password` (ROLE_ADMIN)

---

### 2. Browse Theatres

```bash
# All theatres
curl http://localhost:8080/api/theatres

# Filter by city
curl "http://localhost:8080/api/theatres?city=Mumbai"
```

---

### 3. Browse Shows

```bash
# Shows for theatre 1 on a specific date
curl "http://localhost:8080/api/shows?theatreId=1&date=2025-04-21"
```

---

### 4. Check Available Seats

```bash
curl http://localhost:8080/api/shows/1/seats
```

---

### 5. Book Seats

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"showId": 1, "seatIds": [1, 2, 3]}'
```

Response includes `totalAmount`, `discountAmount`, and `finalAmount` reflecting applied discounts.

---

### 6. View My Bookings

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/bookings/my?page=0&size=10"
```

---

### 7. Cancel a Booking

```bash
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/bookings/1?reason=Change+of+plans"
```

Seats are released back to AVAILABLE. A `BookingCancelledEvent` TODO marks where a refund trigger would go.

---

### 8. Pay for a Booking

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookingId": 1, "paymentMethod": "UPI"}'
```

Accepted methods: `UPI`, `CARD`, `NET_BANKING`. Payment is currently mocked (90% success rate). A TODO marks the Razorpay/Stripe integration point.

---

### 9. Admin — Create Theatre *(ROLE_ADMIN only)*

```bash
# Login as admin first
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.token')

curl -X POST http://localhost:8080/api/admin/theatres \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Cineplex Gold", "city": "Pune", "address": "FC Road", "totalSeats": 80}'
```

---

### 10. Admin — Create Show *(ROLE_ADMIN only)*

```bash
curl -X POST http://localhost:8080/api/admin/shows \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"movieName": "Inception", "theatreId": 1, "showTime": "2026-05-01T15:30:00", "ticketPrice": 250, "totalSeats": 80}'
```

Seats are auto-generated (rows A–H, columns 1–10). Delete a show with `DELETE /api/admin/shows/{showId}`.

---

## Discount Rules

| Strategy | Trigger | Discount |
|---|---|---|
| Third Ticket | Booking 3+ seats | 50% off the 3rd ticket price |
| Afternoon Show | Show between 12:00–17:00 | 20% off total |
| Both | 3+ seats for afternoon show | Both stack — added together |

**Example:** 3 tickets × ₹300 for a 14:30 show
- Third ticket: ₹150
- Afternoon (20% of ₹900): ₹180
- Total discount: ₹330 → **Final: ₹570**

---

## Concurrency: Seat Locking

Concurrent booking requests for the same seats are handled with `PESSIMISTIC_WRITE` database locks. When two requests arrive simultaneously for seat A1, one acquires the lock, books the seat, and commits. The second sees `BOOKED` status and gets a `409 Conflict` response.

This is simpler than Redis-based TTL locks but safe for a single-node deployment. For multi-node, a Redis lock with expiry would be the right next step.

---

## Non-Functional Requirements — Architecture & Design

### 1. Transactional Scenarios

**Seat booking (implemented):**
`PESSIMISTIC_WRITE` lock is acquired on the seat rows inside a single `@Transactional` boundary. Two concurrent requests for seat A1 — one blocks, reads `BOOKED`, and fails with 409. This is intentional: contention on popular shows is high, so optimistic locking would cause retry storms at peak time.

**Payment + booking decoupled:**
Booking is created in state `CONFIRMED` before payment. Payment is a separate transaction. If payment fails the customer can retry without re-selecting seats. If booking is cancelled after a successful payment, a `BookingCancelledEvent` (TODO: Kafka) triggers the refund asynchronously — the refund does not block the cancellation response.

**Bulk operations:**
`cancelBookingsBulk` wraps each individual `cancelBooking` in its own `@Transactional` call. A failure on one booking does not roll back others — partial success is returned in the response (`cancelled`, `skipped`, `skippedIds`). This is the right tradeoff: bulk cancel should be best-effort, not all-or-nothing.

**Read-only transactions:**
All read operations (`getBooking`, `getUserBookings`, `getShowsByTheatreAndDate`) are annotated `@Transactional(readOnly = true)`. Hibernate skips dirty-checking for these, reducing overhead by ~30% under load.

---

### 2. Integration with Theatres (Existing IT Systems, New Theatres, Localization)

**Existing IT systems (heterogeneous integrations):**
Define a `TheatreIntegrationPort` interface with methods like `pushShowUpdate(ShowEvent)` and `pullInventory(theatreId)`. Implement one adapter per vendor:
- **PVR / INOX**: REST adapter calling their proprietary APIs
- **Legacy POS systems**: SFTP-based batch adapter parsing nightly CSV exports
- **New cloud-native theatres**: webhook adapter — they POST show events to `/api/webhooks/shows`; we validate the HMAC signature and upsert

This keeps the core domain isolated from integration details (Hexagonal Architecture / Ports & Adapters).

**New theatre onboarding:**
Admin B2B APIs (already implemented) handle self-service onboarding. In production, this would extend to: API key issuance per theatre, rate limits per tier, and an onboarding wizard that provisions seats and syncs an initial show schedule.

**Localization:**
- Movie names: store `movieNameOriginal` + `movieNameLocalized` (e.g., Hindi title for Bollywood)
- Pricing: `BigDecimal amount` + `String currencyCode` (ISO 4217 — INR, USD, SGD)
- Dates/times: persist as UTC in the DB; convert to theatre's local timezone at the API layer using `ZoneId` stored on the Theatre entity
- Language: `Accept-Language` header on API responses drives localized show descriptions and notification templates

---

### 3. Scale to Multiple Cities / Countries — 99.99% Availability

**99.99% target = ~52 minutes downtime per year.**

**Stateless design (already in place):** JWT-based auth means any node handles any request — horizontal scaling behind an ALB with no sticky sessions.

**Tiered scaling strategy:**

| Layer | Approach |
|---|---|
| API | Stateless pods behind ALB; auto-scale on CPU/RPS |
| DB writes | PostgreSQL primary per region; PgBouncer for connection pooling |
| DB reads | Read replicas; `@Transactional(readOnly=true)` routed to replica |
| Seat availability | Redis cache (TTL 30s); invalidated on every booking/cancellation |
| Show listings | CDN-cached at edge for anonymous browsing (GET /api/shows) |
| Payment calls | Circuit breaker (Resilience4j) with 3-retry + exponential backoff |

**Multi-region:**
- Active-active reads: show listings served from nearest region via CDN
- Active-passive writes: bookings go to primary region; cross-region replication (sync for booking, async for analytics)
- Data residency: India bookings stored in `ap-south-1`; GDPR-covered EU data stays in `eu-west-1`

**Availability mechanisms:**
- Blue-green deployments: zero-downtime via ALB target group switch
- DB failover: RDS Multi-AZ with automatic failover (~60s); covers node-level failure
- Chaos engineering: scheduled fault injection (kill random pod, delay payment gateway) to validate recovery paths

---

### 4. Payment Gateway Integration

**Current state:** 90% mock in `PaymentServiceImpl` with a TODO marking the integration point.

**Production design:**
Abstract behind a `PaymentGatewayPort` interface:
```
interface PaymentGatewayPort {
    PaymentOrderResponse createOrder(BigDecimal amount, String currency);
    PaymentVerifyResponse verifyWebhook(String payload, String signature);
    RefundResponse refund(String transactionId, BigDecimal amount);
}
```
Implementations: `RazorpayGateway`, `StripeGateway`, `PayUGateway` — switchable per country via config.

**Flow:**
1. `POST /api/payments` creates a gateway order, returns a `paymentUrl` to the client
2. Client completes payment on gateway-hosted page (no raw card data touches our server)
3. Gateway POSTs webhook to `/api/webhooks/payment`; we verify HMAC signature, update `Payment.status`
4. Idempotency: `transactionId` is unique in DB; gateway receives our `bookingId` as idempotency key

**PCI-DSS:** card data never stored on our side — gateway tokenizes it. We store only the gateway token (`cardToken`) for saved cards.

---

### 5. Monetization

| Revenue stream | Mechanism |
|---|---|
| **Convenience fee** | 2–3% per booking (like BookMyShow) — added to `finalAmount` |
| **B2B SaaS** | Monthly fee per theatre for API access, analytics dashboard, CRM sync |
| **Premium listings** | Theatres pay for "featured" placement in city search results |
| **Ad inventory** | Sponsored banners on high-traffic show search results |
| **Data products** | Anonymized booking trend reports sold to studios for release planning |
| **Loyalty / subscriptions** | Monthly pass (fixed N bookings/month at flat fee) — drives retention |

Convenience fee is implemented as an additional `DiscountStrategy` in reverse — a `ConvenienceFeeStrategy` that adds a fixed percentage. Using the existing strategy pattern means it composes cleanly with existing discounts.

---

### 6. OWASP Top 10 Protection

| Threat | Mitigation (implemented / planned) |
|---|---|
| **A01 Broken Access Control** | JWT RBAC (ROLE_CUSTOMER/ADMIN); ownership check on every booking/payment operation before action |
| **A02 Cryptographic Failures** | HTTPS enforced (HSTS header); JWT signed HS256; TODO: rotate to RS256 + store secret in Vault |
| **A03 Injection** | JPA parameterized queries throughout — no string-concatenated SQL; `@Valid` on all `@RequestBody` |
| **A04 Insecure Design** | `seatIds` list bounded by show capacity; booking ownership validated before every write |
| **A05 Security Misconfiguration** | Security headers added (HSTS, X-Frame-Options, X-Content-Type-Options, CSP); CORS restricted to known origins |
| **A06 Vulnerable Components** | Spring Boot BOM manages transitive dependencies; Dependabot for CVE alerts |
| **A07 Auth Failures** | JWT expiry 24h; TODO: refresh token rotation; account lockout after N failed logins |
| **A08 Integrity Failures** | Signed JWTs; idempotency key sent to payment gateway; webhook HMAC signature verification |
| **A09 Logging Failures** | SLF4J structured logging on all state transitions; TODO: audit log for payment and cancellation events |
| **A10 SSRF** | No user-controlled URLs in outbound calls; payment gateway URLs are config-only, not request-driven |

Rate limiting (100 req/min per IP) via API gateway (Kong/AWS WAF) sits in front of all endpoints — not implemented in-process to keep the service layer thin.

---

### 7. Compliance

**PCI-DSS (Payment Card Industry):**
- Never store raw PANs or CVVs — gateway tokenizes card data
- Quarterly ASV vulnerability scans required at Level 2+ merchant volume
- Annual penetration test; SAQ-A or SAQ-D depending on integration model

**GDPR / India DPDP Act 2023:**
- Collect only necessary PII (email, not phone/DOB unless required)
- Booking records anonymized after 2 years (userId replaced with hash)
- Right to erasure: `DELETE /api/users/{id}` cascades to bookings, payments, and user profile
- Data Processing Agreements (DPAs) required with all theatre partners
- Indian user data stored in `ap-south-1` (data residency requirement under DPDP)

**SOC 2 Type II (for enterprise theatre partners):**
- Access control audit trail for admin operations
- Change management: all schema changes via Flyway with peer review
- Incident response SLA: P1 booking failures acknowledged within 15 minutes

---

## What I'd Add Next

- **Flyway migrations** — replace `ddl-auto=create` with versioned SQL migrations for safe schema evolution
- **DB-backed users** — User entity with BCrypt passwords, registration flow, and role management
- **Real payment gateway** — replace the 90% mock in `PaymentServiceImpl` with Razorpay/Stripe SDK (TODO marks the integration point)
- **Kafka events** — `BookingConfirmedEvent` and `BookingCancelledEvent` for async email notifications and refund initiation (TODOs in `BookingServiceImpl`)
- **Redis seat lock TTL** — for multi-node deployments: optimistic reserve-then-confirm with a 10-minute expiry, falling back to PESSIMISTIC_WRITE only on conflict
- **Distributed tracing** — propagate `bookingId` as a trace ID via OpenTelemetry + Jaeger across the payment and notification paths
- **Refund flow** — tie `BookingCancelledEvent` to a `PaymentRefundService` that calls the gateway refund API and updates `Payment.status = REFUNDED`
