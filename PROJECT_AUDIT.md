# RIMS System Architecture & Technical Audit Report
**Project Name:** Retail Inventory Management System (RIMS)  
**Project Code:** `PRG_RIMS_001`  
**Client:** XYZ Retail Corporation  
**Audit Date:** October 28, 2026  
**Auditor:** Lead System Architect & Compliance Engineering  

---

## 1. Executive Summary

XYZ Retail Corporation is transitioning from fragmented, store-isolated legacy systems to an enterprise-grade, cloud-ready Retail Inventory Management System (RIMS). The system manages inventory across a network of 500+ physical retail stores and regional fulfillment centers, supporting three core merchandise categories: **Apparel**, **Home Goods**, and **Electronics**.

This technical audit report provides a comprehensive review of the entire system architecture, container topologies, technology inventory, security controls, and compliance posture against the official RIMS Product Backlog (UST R1 through UST R7), including the targeted remediation and verification pass.

---

## 2. System Architecture

RIMS is engineered as a decoupled, multi-tier enterprise architecture consisting of:
1. **Frontend Presentation Tier:** Single Page Application (SPA) built with Angular 21, Vite build engine, HTML5, CSS3, and modern Reactive State Signals.
2. **Backend Services Tier:** RESTful API services built with Java 17, Spring Boot 4.1.1, Spring Data JPA, and Spring Security 6.
3. **Persistence Tier:** Relational Database Management System running PostgreSQL 16 Alpine, configured with ACID-compliant relational integrity, indexes, and transactional boundaries. (An isolated H2 database in PostgreSQL compatibility mode is configured for unit/integration testing).
4. **Orchestration & Containerization:** Docker and Docker Compose environment providing repeatable builds, isolated container networks (`rims-net`), and health checks.

```
                      +─────────────────────────────────────────+
                      |         Web Client Browser              |
                      |  (Angular 21 SPA / Signals / Router)    |
                      +────────────────────┬────────────────────+
                                           | HTTP / REST (JSON)
                                           | Port 8080 (Proxy)
                                           v
                      +─────────────────────────────────────────+
                      |       Spring Boot 4 Backend API         |
                      |  - Spring Security (JWT / RBAC)         |
                      |  - REST Controllers (OpenAPI / Swagger) |
                      |  - Transactional Service Layer          |
                      |  - Spring Data JPA Repositories         |
                      +────────────────────┬────────────────────+
                                           | JDBC / TCP (Port 5432)
                                           | Hikari Connection Pool
                                           v
                      +─────────────────────────────────────────+
                      |       PostgreSQL 16 Database            |
                      |  - 9 Relational Entities & Indexes      |
                      |  - Stock Movement Audit Ledger          |
                      |  - Foreign Keys & Check Constraints     |
                      +─────────────────────────────────────────+
```

---

## 3. Technology Stack Inventory

| Component / Layer | Technology / Version | Deployment Target / Ports | Purpose / Responsibility |
|:---|:---|:---|:---|
| **Frontend Framework** | Angular 21.2.0 (Standalone Components) | Dev: `http://localhost:4200` | User interface, reactive tables, dynamic forms, barcode scanner |
| **Frontend Tooling** | Vitest 4.1 / JSDOM / Angular CLI | Node.js v22 runtime | Rapid compilation, test execution, tree-shaking |
| **Backend Runtime** | Java 17 bytecode (JDK 24 compiler) | Dev & Prod: `http://localhost:8080` | Business logic, data transformation, REST APIs |
| **Backend Framework** | Spring Boot 4.1.1 / Spring Framework 6 | Embedded Tomcat 10 / Spring MVC | IoC container, MVC routing, JPA ORM, validation |
| **Security & Auth** | Spring Security 6 / JJWT 0.12.6 | Stateless JWT Filters | RBAC (ADMIN, STORE_MANAGER, WAREHOUSE_STAFF, AUDITOR) |
| **Database Engine** | PostgreSQL 16 (Alpine Container) / H2 (Test) | Port `5432` (`rims-db`) | Relational persistence, transactional safety, check constraints |
| **Container Engine** | Docker Compose v3.9 | Docker Desktop / Linux / Windows | Containerized multi-service orchestration |
| **API Specification** | OpenAPI 3.0 / Springdoc UI 2.8.5 | `http://localhost:8080/swagger-ui.html` | Interactive API documentation and endpoint testing |

---

## 4. Port Configuration Integrity (Preservation of Port 8080)

A critical requirement established during the infrastructure audit is that the Spring Boot backend runs locally and containerized on port **8080**, and all reverse proxies and frontend configurations strictly route to port **8080**:
- `auth-backend/src/main/resources/application.properties`: `server.port=${SERVER_PORT:8080}` (Verified)
- `angular-auth/proxy.conf.json`: Target set to `http://127.0.0.1:8080` (Verified)
- `auth-backend/Dockerfile`: `EXPOSE 8080` (Verified)
- `docker-compose.yml`: Backend service maps `"8080:8080"` and health check probes `http://localhost:8080/actuator/health` (Verified)
- `angular-auth/src/app/layout/sidebar/sidebar.html`: Displays API Target as `localhost:8080` (Verified)
- `angular-auth/src/app/features/dashboard/dashboard.ts`: Error fallback message references `port 8080` (Verified)
- `.env.example`: Standardized backend port to `8080` (remediated previous 8083 typo).

---

## 5. Architectural Findings & Remediation History

### 5.1 CI/CD Pipeline & Test Execution Hardening (Task 1)
- **Previous State:** Pipeline in `.github/workflows/ci-cd.yml` suppressed test failures using `|| true` in both backend and frontend jobs, ran `clean package -DskipTests` deleting Surefire reports, and passed obsolete Karma arguments (`--browsers=ChromeHeadless`) to the Angular Vitest test runner.
- **Remediation:**
  - Removed `|| true` from all test commands.
  - Configured frontend test step with `npm test -- --no-watch`.
  - Reconfigured backend build to run `./mvnw -B test` followed by `./mvnw -B package -DskipTests` (without clean wiping out test reports), preserving Surefire artifact uploads.
  - Added H2 in-memory test dependency to `pom.xml` with isolated test configuration in `src/test/resources/application.properties`, enabling 100% reliable CI test runs without external PostgreSQL dependencies.

### 5.2 Stock Transfer Request Validation & Concurrency Protection (Task 2)
- **Previous State:** `transferStock` lacked comprehensive validation for null/zero/negative quantities, missing IDs, or identical warehouses. Concurrency was vulnerable to lost updates because inventory lookups did not acquire pessimistic row locks.
- **Remediation:**
  - Added strict programmatic guard clauses in `InventoryService.transferStock()` rejecting null requests, missing product/warehouse IDs, zero or negative quantities, and identical source/destination warehouses.
  - Implemented `@Lock(LockModeType.PESSIMISTIC_WRITE)` queries in `InventoryRepository` (`findByProductProductIdAndWarehouseWarehouseIdWithLock`) to ensure serialized inventory updates.
  - Verified available unreserved stock (`quantity - reservedQuantity`) before deduction.
  - Enforced atomic transaction rollback on failure so `stock_movement` records are saved strictly when the transfer succeeds.
  - Added 9 unit regression tests in `InventoryServiceTest.java`.

### 5.3 Order Allocation & Fulfillment Concurrency (Task 3)
- **Previous State:** `confirmOrder()`, `shipOrder()`, and `cancelOrder()` used standard non-locking queries. Shipping and cancellation used `Math.max(0, ...)` silent clamping which masked inventory underflow bugs. Concurrent orders could double-reserve inventory.
- **Remediation:**
  - Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` query in `OrderRepository` (`findByIdWithLock`).
  - Added pessimistic locks on all line item inventory rows during confirmation, shipping, and cancellation.
  - Sorted order items deterministically prior to locking to prevent database deadlocks.
  - Enforced strict state transitions (`DRAFT` required for confirmation; prevents double reservation).
  - Replaced silent clamping in `shipOrder()` and `cancelOrder()` with explicit invariant checks throwing `InvalidOrderException` on underflows.
  - Ensured cancellation decrements reservations exactly once and marks order `CANCELLED`.
  - Created `OrderServiceTest.java` with 19 comprehensive unit and simulation tests.

### 5.4 Secure Configuration & Secret Management (Task 4)
- **Previous State:** Hardcoded database password (`1909`) and default JWT secret were committed into `docker-compose.yml`, `application.properties`, and `.env.example`.
- **Remediation:**
  - Replaced hardcoded credentials in `docker-compose.yml` and `application.properties` with environment variables (`${SPRING_DATASOURCE_PASSWORD:postgres}`, `${RIMS_JWT_SECRET:...}`).
  - Added defense-in-depth secret validation in `JwtUtil` requiring at least 32 characters (256 bits).
  - Updated `.env.example` with safe placeholder variables and port 8080.
  - Updated `.gitignore` to prevent committing `.env`, `.env.*`, `*.pem`, and `*.key`.

### 5.5 Database Integrity & Safe Migration (Task 5)
- **Previous State:** `database/schema.sql` began with unconditional `DROP TABLE IF EXISTS ... CASCADE`, risking data loss during setup or restarts. Schema types contained minor mismatches with JPA entities.
- **Remediation:**
  - Removed destructive `DROP TABLE` statements from `schema.sql`, replacing them with idempotent `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS`.
  - Separated teardown logic into `database/reset.sql`.
  - Authored `database/MIGRATION_STRATEGY.md` outlining zero-destructive DDL principles and upgrade paths.
  - Aligned table definitions with JPA entities (e.g. `order_item.warehouse_id` nullable, customer name/email field widths).

---

## 6. Audit Conclusion

The RIMS architecture is robust, highly decoupled, and compliant with XYZ Retail Corporation's enterprise requirements. Data consistency across inventory mutations is protected by database check constraints, pessimistic row locks (`SELECT ... FOR UPDATE`), and Spring `@Transactional` boundaries. Authentication and authorization are securely enforced via BCrypt hashing, stateless JWT tokens, and Spring Security method-level security.

---

## 7. Known Issues & Deferred Work

- **First-Login HTTP 403 (Deferred):** An intermittent HTTP 403 status code was previously observed on initial login attempts in specific browser/proxy timing contexts before auth token synchronization completes. This behavior is documented and deferred for isolated root cause investigation; no speculative modifications were made to ensure authentication stability and security posture remain intact.
