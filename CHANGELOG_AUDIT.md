# RIMS Granular Change Log & Audit Trail
**Project Code:** `PRG_RIMS_001`  
**Client:** XYZ Retail Corporation  
**Audit Period:** October 28, 2026  
**Scope:** Remediation of UST R1 – UST R7 Compliance Gaps  

---

## 1. Summary of Changes

A total of 32 files were modified or created across the backend (`auth-backend`), frontend (`angular-auth`), infrastructure configurations, and documentation. All modifications strictly respect the architectural safeguard preserving **Port 8080**, ensuring transactional integrity across inventory transfers and order allocations, enforcing granular RBAC, and resolving JSON serialization exceptions.

---

## 2. Granular File Modification Log

### 2.1 Infrastructure & Environment Configurations
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `auth-backend/Dockerfile` | Modified | Updated exposed port from 8083 to `8080` to preserve the unified API port architecture. |
| `docker-compose.yml` | Modified | Corrected backend port mapping to `"8080:8080"` and health check probe to `http://localhost:8080/actuator/health`. |
| `README.md` | Modified | Standardized all documentation instructions, Docker commands, and curl examples to port `8080`. |
| `angular-auth/angular.json` | Modified | Added `"fonts": false` under build optimization to prevent network timeout failures when building offline in sandboxed environments. |
| `angular-auth/proxy.conf.json` | Verified | Maintained proxy target as `http://127.0.0.1:8080` rewriting `/api` to root backend context. |
| `auth-backend/src/main/resources/application.properties` | Verified | Maintained `server.port=8080` as the single authoritative source of truth. |

---

### 2.2 Backend Domain & Service Layer (`auth-backend`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `.../config/DataInitializer.java` | Modified | Added SQL safety check to drop legacy enum constraints during DB initialization, preventing seed failure with new roles. |
| `.../config/SecurityConfig.java` | Modified | Added `@EnableMethodSecurity`, configured `.requestMatchers("/users/**").hasRole("ADMIN")`, and retained public access for `/auth/login` and Swagger UI. |
| `.../filter/JwtAuthFilter.java` | Modified | Added role authority extraction adding both `ROLE_<ROLE>` and `<ROLE>` to Spring Security's `SecurityContextHolder`. |
| `.../enums/OrderStatus.java` | Modified | Extended enum values with `PICKED`, `PACKED`, and `SHIPPED` to support the complete order fulfillment pipeline. |
| `.../entity/Inventory.java` | Modified | Added `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` to eliminate HTTP 500 Jackson cyclic serialization errors. |
| `.../entity/Product.java` | Modified | Added `@JsonIgnoreProperties` annotations to eliminate lazy-loading proxy serialization exceptions. |
| `.../entity/Warehouse.java` | Modified | Added `@JsonIgnoreProperties` annotations to eliminate lazy-loading proxy serialization exceptions. |
| `.../dto/StockTransferRequest.java` | Created | Added Java record/DTO encapsulating `productId`, `sourceWarehouseId`, `targetWarehouseId`, `quantity`, and `reason`. |
| `.../service/InventoryService.java` | Modified | Implemented `@Transactional transferStock(StockTransferRequest)` with source stock decrement, target stock increment, balance validation, and dual `StockMovement` ledger generation. |
| `.../service/OrderService.java` | Modified | Implemented stock reservations in `confirmOrder()`, setting `Inventory.reservedQuantity` and `OrderItem.status = ALLOCATED`. Added `pickOrder()`, `packOrder()`, `shipOrder()` (decrements physical and reserved stock, logs `OUTBOUND_SALE`), and `cancelOrder()` (releases reserved stock). |
| `.../service/ProductService.java` | Modified | Implemented `bulkImportFromCsv(MultipartFile)` with Apache Commons CSV validation and batch saves; implemented `exportToCsv()` streaming product catalog. |
| `.../service/PurchaseOrderService.java` | Modified | Injected `StockMovementRepository` and added audit ledger entry (`INBOUND_PO`) when receiving purchase order goods. |

---

### 2.3 Backend Controller Layer (`auth-backend`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `.../controller/AppUserController.java` | Modified | Added `@PreAuthorize("hasRole('ADMIN')")` at class level to enforce administrative authorization. |
| `.../controller/InventoryController.java` | Modified | Added `POST /inventory/transfer` endpoint mapping to `InventoryService.transferStock`. |
| `.../controller/OrderController.java` | Modified | Added status transition endpoints: `PUT /orders/{id}/pick`, `PUT /orders/{id}/pack`, `PUT /orders/{id}/ship`, and `PUT /orders/{id}/cancel`. |
| `.../controller/ProductController.java` | Modified | Added `POST /products/bulk-import` (multipart) and `GET /products/export/csv` (text/csv). |

---

### 2.4 Frontend Services, Guards & Routing (`angular-auth`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `src/app/core/guards/auth.guard.ts` | Modified | Updated `authGuard` to read `route.data['roles']` and verify against `currentUser()?.role`; redirects unauthorized users to `/dashboard`. |
| `src/app/app.routes.ts` | Modified | Configured `data: { roles: ['ADMIN'] }` on `/users` route. |
| `src/app/core/models/product.model.ts` | Modified | Replaced empty interface with fully typed `Product` TypeScript model. |
| `src/app/core/services/product.ts` | Modified | Implemented typed service methods: `getAll`, `getById`, `create`, `update`, `delete`, `searchByName`, `searchByCategory`, `searchByBrand`, `bulkImport`, and `exportCsv`. |
| `src/app/core/services/inventory-api.ts` | Modified | Added generic typed `get<T>(endpoint: string)` method to support custom analytics payloads. |

---

### 2.5 Frontend Presentation & UI Components (`angular-auth`)
| File Path | Action | Description of Modifications |
|:---|:---:|:---|
| `src/app/features/dashboard/dashboard.ts` | Modified | Injected analytics API endpoints (`analytics/kpis` and `analytics/categories`) via RxJS `forkJoin`, added INR currency and number formatters. |
| `src/app/features/dashboard/dashboard.html` | Modified | Added 4 executive KPI cards (Inventory Valuation, Stockout Rate, Accuracy Rate, Units On Hand) and Category Distribution breakdown table. |
| `src/app/features/dashboard/dashboard.css` | Modified | Added CSS rules for executive KPI badge symbols and responsiveness. |
| `src/app/features/resource-config.ts` | Modified | Added Confirm, Pick, Pack, Ship, and Cancel lifecycle action buttons to order management view. |
| `src/app/layout/sidebar/sidebar.ts` | Modified | Injected `AuthService` and exposed `isAdmin = computed(() => this.authService.currentUser()?.role === 'ADMIN')`. |
| `src/app/layout/sidebar/sidebar.html` | Modified | Wrapped Administration / Users navigation section in `@if (isAdmin())` conditional block. |
| `src/app/shared/components/resource-workspace/resource-workspace.ts` | Modified | Added low-stock checkbox filter and status badge evaluator. |
| `src/app/shared/components/resource-workspace/resource-workspace.html` | Modified | Added low-stock visual warning badge (`[Low Stock]`) and filter toggle on `/inventory`. |

---

## 3. Byte-Level Character Sanitation
- Removed UTF-8 Byte Order Marks (`\uFEFF`) that had been introduced into Java controllers during earlier Windows text modifications, restoring clean Java compiler compatibility across all 87 source files.
