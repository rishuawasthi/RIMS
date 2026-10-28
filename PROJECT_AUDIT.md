# RIMS System Architecture & Technical Audit Report
**Project Name:** Retail Inventory Management System (RIMS)  
**Project Code:** `PRG_RIMS_001`  
**Client:** XYZ Retail Corporation  
**Audit Date:** October 28, 2026  
**Auditor:** Lead System Architect & Compliance Engineering  

---

## 1. Executive Summary

XYZ Retail Corporation is transitioning from fragmented, store-isolated legacy systems to an enterprise-grade, cloud-ready Retail Inventory Management System (RIMS). The system manages inventory across a network of 500+ physical retail stores and regional fulfillment centers, supporting three core merchandise categories: **Apparel**, **Home Goods**, and **Electronics**.

This technical audit report provides a comprehensive review of the entire system architecture, container topologies, technology inventory, security controls, and compliance posture against the official RIMS Product Backlog (UST R1 through UST R7).

---

## 2. System Architecture

RIMS is engineered as a decoupled, multi-tier enterprise architecture consisting of:
1. **Frontend Presentation Tier:** Single Page Application (SPA) built with Angular 21, Vite build engine, HTML5, CSS3, and modern Reactive State Signals.
2. **Backend Services Tier:** RESTful API services built with Java 17, Spring Boot 4.1.1, Spring Data JPA, and Spring Security 6.
3. **Persistence Tier:** Relational Database Management System running PostgreSQL 16 Alpine, configured with ACID-compliant relational integrity, indexes, and transactional boundaries.
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
| **Frontend Framework** | Angular 21.0.0 (Standalone Components) | Dev: `http://localhost:4200` | User interface, reactive tables, dynamic forms, barcode scanner |
| **Frontend Tooling** | Vite / Angular CLI / Vitest | Node.js v20+ runtime | Rapid compilation, test execution, tree-shaking |
| **Backend Runtime** | Java 17 / Eclipse Temurin JDK 17 | Dev & Prod: `http://localhost:8080` | Business logic, data transformation, REST APIs |
| **Backend Framework** | Spring Boot 4.1.1 / Spring Framework 6 | Embedded Tomcat 10 / Spring MVC | IoC container, MVC routing, JPA ORM, validation |
| **Security & Auth** | Spring Security 6 / JJWT 0.12.5 | Stateless JWT Filters | RBAC (ADMIN, STORE_MANAGER, WAREHOUSE_STAFF, AUDITOR) |
| **Database Engine** | PostgreSQL 16 (Alpine Container) | Port `5432` (`rims-db`) | Relational persistence, transactional safety, check constraints |
| **Container Engine** | Docker Compose v3.9 | Docker Desktop / Linux / Windows | Containerized multi-service orchestration |
| **API Specification** | OpenAPI 3.0 / Springdoc UI | `http://localhost:8080/swagger-ui.html` | Interactive API documentation and endpoint testing |

---

## 4. Port Configuration Integrity (Preservation of Port 8080)

A critical requirement established during the infrastructure audit is that the Spring Boot backend runs locally and containerized on port **8080**, and all reverse proxies and frontend configurations strictly route to port **8080**:
- `auth-backend/src/main/resources/application.properties`: `server.port=8080` (Verified)
- `angular-auth/proxy.conf.json`: Target set to `http://127.0.0.1:8080` (Verified)
- `auth-backend/Dockerfile`: `EXPOSE 8080` (Verified)
- `docker-compose.yml`: Backend service maps `"8080:8080"` and health check probes `http://localhost:8080/actuator/health` (Verified)
- `angular-auth/src/app/layout/sidebar/sidebar.html`: Displays API Target as `localhost:8080` (Verified)
- `angular-auth/src/app/features/dashboard/dashboard.ts`: Error fallback message references `port 8080` (Verified)

---

## 5. Architectural Findings & Remediation History

### 5.1 Product Catalog Bulk Ingestion & Export (UST R1)
- **Previous State:** Product management supported single record CRUD. Bulk import and CSV export were missing.
- **Remediation:** 
  - Implemented `ProductService.bulkImportFromCsv(MultipartFile)` parsing Apache Commons CSV, validating headers (`Product Name`, `SKU`, `Category`, `Price`, `Unit`, `Brand`), verifying price validity, and saving in batches.
  - Implemented `ProductService.exportToCsv()` generating structured CSV output of all catalog items.
  - Exposed `POST /products/bulk-import` and `GET /products/export/csv` in `ProductController`.
  - Implemented full typed API client methods in `angular-auth/src/app/core/services/product.ts`.

### 5.2 Multi-Location Inventory & Stock Transfers (UST R2 & UST R3)
- **Previous State:** `/inventory` returned HTTP 500 due to lazy initialization and cyclic Jackson serialization between `Inventory`, `Product`, and `Warehouse`. Stock transfers between warehouses were not supported.
- **Remediation:**
  - Resolved serialization errors on `Inventory`, `Product`, and `Warehouse` using `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` and eager relationship mapping on business-critical identifiers.
  - Implemented `@Transactional transferStock(StockTransferRequest)` in `InventoryService`:
    - Validates source warehouse stock sufficiency (`sourceInv.getQuantity() >= transferQty`).
    - Decrements source inventory and creates or increments target inventory.
    - Records two atomic ledger entries in `stock_movement`: `TRANSFER_OUT` and `TRANSFER_IN`.
  - Added low-stock visual badges (`[Low Stock]`) and a quick-filter toggle in the Angular `resource-workspace` UI.

### 5.3 Order Fulfillment & Allocation Engine (UST R4)
- **Previous State:** Customer orders were created in `DRAFT` status with no inventory allocation or lifecycle tracking.
- **Remediation:**
  - Updated `OrderService.confirmOrder()`: Evaluates inventory on hand across line item warehouses, locks the required stock by incrementing `Inventory.reservedQuantity`, and transitions item status to `ALLOCATED`.
  - Implemented full order lifecycle transition methods: `pickOrder()` (`PICKED`), `packOrder()` (`PACKED`), `shipOrder()` (`SHIPPED` — decrements `quantity` and `reservedQuantity`, logs `OUTBOUND_SALE`), and `cancelOrder()` (`CANCELLED` — releases reserved stock).
  - Exposed transition endpoints: `/orders/{id}/pick`, `/orders/{id}/pack`, `/orders/{id}/ship`, `/orders/{id}/cancel`.
  - Wired action buttons in the Angular order management grid.

### 5.4 Procurement & Goods Receipt Auditing (UST R5)
- **Previous State:** Receiving purchase orders updated product inventory counts, but did not log audit transactions.
- **Remediation:**
  - Updated `PurchaseOrderService.receivePurchaseOrder()` to inject `StockMovementRepository` and log a structured `INBOUND_PO` audit record in `stock_movement` with reference number, quantity received, and receiving timestamp.

### 5.5 Executive Analytics & Category KPIs (UST R6)
- **Previous State:** Backend possessed an `AnalyticsController` computing valuation and category breakdowns, but the Angular frontend dashboard only displayed hardcoded stubs or basic table counts.
- **Remediation:**
  - Updated `dashboard.ts` to query `/api/analytics/kpis` and `/api/analytics/categories` concurrently via RxJS `forkJoin`.
  - Updated `dashboard.html` and `dashboard.css` with 4 executive KPI cards (Inventory Valuation in INR, Stockout Rate %, Inventory Accuracy %, Total Units on Hand / Reserved) and an expandable Category Valuation breakdown table.

### 5.6 Granular RBAC & Security Hardening (UST R7)
- **Previous State:** Route guards checked token presence but ignored role authorization; admin endpoints lacked method-level security.
- **Remediation:**
  - Annotated `SecurityConfig.java` with `@EnableMethodSecurity` and restricted `/users/**` to `hasRole('ADMIN')`.
  - Annotated `AppUserController.java` with `@PreAuthorize("hasRole('ADMIN')")`.
  - Updated Angular `authGuard` to inspect `route.data['roles']` and redirect unauthorized roles to `/dashboard`.
  - Added role protection to `/users` route in `app.routes.ts` (`data: { roles: ['ADMIN'] }`).
  - Updated `Sidebar` component to inject `AuthService` and conditionally hide the Administration/Users navigation section for non-admin users.

---

## 6. Audit Conclusion

The RIMS architecture is robust, highly decoupled, and compliant with XYZ Retail Corporation's enterprise requirements. Data consistency across inventory mutations is protected by database check constraints and `@Transactional` boundaries, while authentication and authorization are securely enforced at both the API gateway and method levels.
