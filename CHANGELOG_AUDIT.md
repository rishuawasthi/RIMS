# RIMS Granular Change Log & Remediation Audit Trail
**Project Code:** `PRG_RIMS_001`  
**Client:** XYZ Retail Corporation  
**Audit Period:** October 28, 2026  
**Scope:** Targeted Remediation and Verification Pass (Tasks 1 through 7)

---

## 1. Summary of Changes

A total of 24 files were modified or created across the backend (`auth-backend`), frontend CI/CD configurations, database scripts, and compliance documentation. All modifications strictly respect the architectural safeguard preserving **Port 8080**, ensuring transactional integrity across inventory transfers and order allocations, enforcing concurrency controls via pessimistic row locks, eliminating test suppression, and protecting database data during upgrades.

---

## 2. Granular File Modification Log

### 2.1 CI/CD & Build Pipeline (`Task 1`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `.github/workflows/ci-cd.yml` | Modified | Removed `|| true` failure suppression from backend and frontend test steps. Configured backend job to run `./mvnw -B test` followed by `./mvnw -B package -DskipTests` (without clean), preserving Surefire artifact uploads. Configured frontend test step with `npm test -- --no-watch`, removing obsolete Karma arguments. |
| `auth-backend/pom.xml` | Modified | Added `com.h2database:h2` dependency in `test` scope. |
| `auth-backend/src/test/resources/application.properties` | Created | Added isolated test profile using in-memory H2 in PostgreSQL compatibility mode, enabling 100% reliable local and CI test execution without requiring external database services. |

---

### 2.2 Domain, Persistence & Service Concurrency (`Tasks 2 & 3`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `.../repository/InventoryRepository.java` | Modified | Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` methods `findByProductProductIdAndWarehouseWarehouseIdWithLock` and `findByIdWithLock`. |
| `.../repository/OrderRepository.java` | Modified | Added `@Lock(LockModeType.PESSIMISTIC_WRITE)` method `findByIdWithLock`. |
| `.../service/InventoryService.java` | Modified | Added strict parameter validation to `transferStock()` (rejection of null requests, missing IDs, zero or negative quantities, and identical source/destination warehouses). Added pessimistic write locking on source and target inventory records, verified unreserved stock, and ensured movements are committed only on success. |
| `.../service/OrderService.java` | Modified | Added pessimistic locking on Order and Inventory rows during confirmation, shipping, and cancellation. Enforced deterministic item ordering to avoid deadlocks. Prevented duplicate order confirmation. Replaced silent quantity clamping (`Math.max(0, ...)`) with explicit invariant underflow checks. Ensured cancellation decrements reservations exactly once. |
| `.../config/DataInitializer.java` | Modified | Standardized warehouse contact numbers to pure digit strings, resolving Jakarta validation violations (`^[0-9]{10,15}$`) during entity persistence. |

---

### 2.3 Automated Test Suites (`Tasks 1, 2 & 3`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `.../service/InventoryServiceTest.java` | Modified | Expanded test suite to 16 tests by adding 9 regression test cases covering stock transfer validation, unreserved stock verification, negative quantity rejection, identical warehouse rejection, and pessimistic lock lookup. |
| `.../service/OrderServiceTest.java` | Created | Created comprehensive unit test suite with 19 tests covering order confirmation reservations, double-confirmation prevention, pick/pack/ship transitions, explicit underflow invariant checks, cancellation release, and concurrent order allocation simulation. |

---

### 2.4 Configuration & Secret Hardening (`Task 4`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `docker-compose.yml` | Modified | Replaced hardcoded database passwords with environment variables (`${POSTGRES_PASSWORD:-postgres}`, `${SPRING_DATASOURCE_PASSWORD:-postgres}`, `${RIMS_JWT_SECRET:...}`). Preserved port 8080 mapping. |
| `auth-backend/src/main/resources/application.properties` | Modified | Externalized sensitive database credentials and JWT signing secret to environment variables. Preserved `server.port=${SERVER_PORT:8080}`. |
| `.../config/JwtUtil.java` | Modified | Added defense-in-depth secret key validation enforcing a minimum length of 32 characters (256 bits). |
| `.env.example` | Modified | Removed real credentials, standardized backend port to 8080 (corrected 8083 typo), updated API URL to 8080, and added clear placeholders. |
| `.gitignore` | Modified | Added `.env.*`, `*.pem`, `*.key` to prevent accidental credential leakage. |

---

### 2.5 Database Schema & Integrity (`Task 5`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `database/schema.sql` | Modified | Removed destructive `DROP TABLE IF EXISTS ... CASCADE`. Converted all table and index declarations to non-destructive `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS`. Aligned column lengths and nullability with JPA entities (`order_item.warehouse_id` nullable, `supplier_name` 150 chars). |
| `database/reset.sql` | Created | Created dedicated, isolated destructive teardown script for explicit reset environments. |
| `database/seed.sql` | Modified | Standardized warehouse contact numbers to pure digit strings matching entity validation rules. |
| `database/MIGRATION_STRATEGY.md` | Created | Authored database migration strategy guide defining non-destructive DDL rules, JPA entity synchronization, and safe upgrade workflows. |

---

### 2.6 Documentation & Compliance Reporting (`Task 6`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `README.md` | Modified | Updated repository URL to `https://github.com/rishuawasthi/RIMS`, removed hardcoded passwords, updated test commands and test counts (58 backend tests, 19 frontend tests), and clarified simulated vs live integrations. |
| `TEST_REPORT.md` | Modified | Rewrote test execution report to reflect actual test runner output, real test names, and passing status across all 58 backend tests and 19 frontend Vitest tests. |
| `REQUIREMENTS_TRACEABILITY.md` | Modified | Updated RTM to map requirements to actual test methods, distinguishing implemented/tested vs simulated components. |
| `DATABASE_DESIGN.md` | Modified | Updated data dictionary, documented non-destructive schema, and referenced migration strategy. |
| `PROJECT_AUDIT.md` | Modified | Updated audit findings with details of the targeted remediation pass. |
| `FINAL_COMPLIANCE_REPORT.md` | Modified | Aligned compliance scorecards with verified evidence, removing unverified 100% production claims. |
