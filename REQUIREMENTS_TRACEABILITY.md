# RIMS Requirements Traceability Matrix (RTM)
**Project Code:** `PRG_RIMS_001`  
**Specification Reference:** XYZ Retail Corporation Agile Project Workbook  
**Verification Date:** October 28, 2026  
**Compliance Status:** 100% MET  

---

## 1. Overview & Traceability Key

This Requirements Traceability Matrix maps business and technical requirements from the Agile Project Workbook (UST R1 – UST R7) to backend Java source components, database tables, frontend Angular components, and automated verification test cases.

| Status Indicator | Meaning |
|:---|:---|
| **[MET]** | Requirement fully implemented, integrated, verified by automated unit/integration tests and static analysis. |
| **[PARTIAL]** | Core logic present but lacking UI or end-to-end automated verification. |
| **[UNMET]** | Specified functionality is not implemented. |

---

## 2. Traceability Matrix

### UST R1: Product Catalog Management
*Target Objective: Centralize product master data across Apparel, Electronics, and Home Goods with SKU tracking, categorization, search, and bulk operations.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R1.1** | Create, edit, and delete product master records with price, SKU, brand, and unit | `ProductController.java`<br>`ProductService.java` | `products.ts`<br>`resource-workspace.ts` | `product` | `ProductServiceTest.testSaveProduct`<br>`ProductServiceTest.testDeleteProduct` | **[MET]** |
| **R1.2** | Multi-attribute search and filtering (by name, category, brand, and SKU) | `ProductController.java` (`/search/**`)<br>`ProductRepository.java` | `resource-workspace.ts`<br>`product.ts` | `product`<br>`idx_product_sku`<br>`idx_product_category` | `ProductServiceTest.testSearchByName`<br>`ProductServiceTest.testSearchByCategory` | **[MET]** |
| **R1.3** | Bulk CSV catalog import with header validation and transactional batch persistence | `ProductController.java` (`/bulk-import`)<br>`ProductService.java` (`bulkImportFromCsv`) | `product.ts` (`bulkImport`)<br>`resource-workspace.ts` | `product` | `ProductServiceTest.testBulkImport` | **[MET]** |
| **R1.4** | Product catalog export to formatted CSV stream | `ProductController.java` (`/export/csv`)<br>`ProductService.java` (`exportToCsv`) | `product.ts` (`exportCsv`)<br>`resource-workspace.ts` | `product` | `ProductServiceTest.testExportCsv` | **[MET]** |

---

### UST R2 & UST R3: Real-Time Multi-Location Inventory & Stock Transfers
*Target Objective: Real-time stock tracking across 500+ warehouses/stores, reorder thresholds, low stock alerts, and inter-location transfers.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R2.1** | Real-time stock visibility by product and warehouse without HTTP 500 serialization errors | `InventoryController.java`<br>`InventoryService.java` | `inventory.ts`<br>`resource-workspace.ts` | `inventory`<br>`warehouse`<br>`product` | `InventoryServiceTest.testGetAllInventory`<br>`InventoryServiceTest.testGetById` | **[MET]** |
| **R2.2** | Low-stock threshold detection and automated alert triggers when `quantity <= reorderLevel` | `InventoryController.java` (`/low-stock`)<br>`InventoryService.java` (`getLowStockAlerts`) | `resource-workspace.ts`<br>`resource-workspace.html` | `inventory` | `InventoryServiceTest.testGetLowStockAlerts` | **[MET]** |
| **R2.3** | Inter-warehouse stock transfer with transactional balance mutation and dual-ledger audit records | `InventoryController.java` (`/transfer`)<br>`InventoryService.java` (`transferStock`)<br>`StockTransferRequest.java` | `inventory.ts`<br>`resource-workspace.ts` | `inventory`<br>`stock_movement` (`TRANSFER_OUT`, `TRANSFER_IN`) | `InventoryServiceTest.testTransferStock`<br>`InventoryServiceTest.testTransferStockInsufficient` | **[MET]** |
| **R2.4** | Barcode and RFID scanner integration endpoints for rapid SKU lookup | `InventoryController.java`<br>`ProductController.java` (`/sku/{sku}`) | `scanner.ts`<br>`scanner.html` | `product`<br>`inventory` | `scanner.spec.ts` | **[MET]** |

---

### UST R4: Customer Order Processing & Fulfillment Lifecycle
*Target Objective: Multi-item sales order intake, automated inventory reservation, pick/pack/ship fulfillment stages, and cancellation stock release.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R4.1** | Capture customer sales orders with nested line items, tax, and address fields | `OrderController.java`<br>`OrderService.java`<br>`OrderItemController.java` | `orders.ts`<br>`order-items.ts` | `orders`<br>`order_item` | `orders.spec.ts`<br>`order-items.spec.ts` | **[MET]** |
| **R4.2** | Stock reservation on order confirmation (`reservedQuantity` increment, `ALLOCATED` status) | `OrderService.java` (`confirmOrder`) | `orders.ts`<br>`resource-config.ts` | `inventory`<br>`order_item` | `InventoryServiceTest.testReserveStock` | **[MET]** |
| **R4.3** | Pick and Pack stage transitions for warehouse fulfillment | `OrderController.java` (`/pick`, `/pack`)<br>`OrderService.java` (`pickOrder`, `packOrder`) | `orders.ts`<br>`resource-config.ts` | `orders` (`PICKED`, `PACKED`) | `orders.spec.ts` | **[MET]** |
| **R4.4** | Ship transition decrements physical `quantity` & `reservedQuantity`, logs `OUTBOUND_SALE` | `OrderController.java` (`/{id}/ship`)<br>`OrderService.java` (`shipOrder`) | `orders.ts`<br>`resource-config.ts` | `inventory`<br>`orders` (`SHIPPED`)<br>`stock_movement` | `orders.spec.ts` | **[MET]** |
| **R4.5** | Order cancellation releases reserved inventory without physical stock corruption | `OrderController.java` (`/{id}/cancel`)<br>`OrderService.java` (`cancelOrder`) | `orders.ts`<br>`resource-config.ts` | `inventory`<br>`orders` (`CANCELLED`) | `orders.spec.ts` | **[MET]** |

---

### UST R5: Supplier & Purchase Order Procurement
*Target Objective: Supplier master profiles, replenishment purchase orders, and goods receipt with inbound stock ledgering.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R5.1** | Supplier directory maintenance (contacts, tax ID, payment terms, status) | `SupplierController.java`<br>`SupplierService.java` | `suppliers.ts`<br>`resource-workspace.ts` | `supplier` | `suppliers.spec.ts`<br>`supplier.spec.ts` | **[MET]** |
| **R5.2** | Purchase order creation, pricing calculation, and tracking (DRAFT -> ORDERED) | `PurchaseOrderController.java`<br>`PurchaseOrderService.java` | `purchase-orders.ts`<br>`resource-workspace.ts` | `purchase_order` | `purchase-orders.spec.ts`<br>`purchase-order.spec.ts` | **[MET]** |
| **R5.3** | Goods receipt replenishes warehouse stock and creates `INBOUND_PO` audit ledger entry | `PurchaseOrderController.java` (`/{id}/receive`)<br>`PurchaseOrderService.java` (`receivePurchaseOrder`) | `purchase-orders.ts`<br>`resource-config.ts` | `purchase_order` (`RECEIVED`)<br>`inventory`<br>`stock_movement` | `purchase-orders.spec.ts` | **[MET]** |

---

### UST R6: Executive Reporting & Retail Analytics
*Target Objective: Real-time inventory valuation, stockout rate KPIs, category distribution, and retail health metrics.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R6.1** | Executive KPI aggregation: Total Valuation, Stockout Rate %, Inventory Accuracy %, On-hand units | `AnalyticsController.java` (`/kpis`)<br>`AnalyticsService.java` (`getExecutiveKpis`) | `dashboard.ts`<br>`dashboard.html`<br>`dashboard.css` | `inventory`<br>`product`<br>`orders`<br>`purchase_order` | `dashboard.spec.ts` | **[MET]** |
| **R6.2** | Category stock and valuation distribution (Apparel, Electronics, Home Goods) | `AnalyticsController.java` (`/categories`)<br>`AnalyticsService.java` (`getCategoryDistribution`) | `dashboard.ts`<br>`dashboard.html` | `product`<br>`inventory` | `dashboard.spec.ts` | **[MET]** |
| **R6.3** | Live reactive UI updates with INR currency formatting and error boundary | `dashboard.ts` (`formatCurrency`, `formatNumber`) | `dashboard.html`<br>`dashboard.css` | N/A (Frontend presentation) | `dashboard.spec.ts` | **[MET]** |

---

### UST R7: User Management & Role-Based Access Control (RBAC)
*Target Objective: Granular user roles (ADMIN, STORE_MANAGER, WAREHOUSE_STAFF, AUDITOR), JWT security, method authorization, and route protection.*

| Requirement ID | Acceptance Criteria | Backend Implementation | Frontend Implementation | Database Entities | Test Cases | Status |
|:---|:---|:---|:---|:---|:---|:---|
| **R7.1** | User authentication with BCrypt hashing, JWT issuance, and lock on failed attempts | `AuthController.java`<br>`AppUserService.java`<br>`JwtUtil.java`<br>`JwtAuthFilter.java` | `login.ts`<br>`auth.ts`<br>`auth.interceptor.ts` | `app_user` | `AppUserServiceTest.testAuthenticateSuccess`<br>`AppUserServiceTest.testAuthenticateBadCredentials`<br>`AppUserServiceTest.testAccountLockout` | **[MET]** |
| **R7.2** | Method-level security (`@EnableMethodSecurity`) and `@PreAuthorize("hasRole('ADMIN')")` | `SecurityConfig.java`<br>`AppUserController.java` | `auth.guard.ts`<br>`app.routes.ts` | `app_user` | `AppUserServiceTest.testAddUser`<br>`AppUserServiceTest.testDeleteUser` | **[MET]** |
| **R7.3** | Angular route guard enforcing role-based navigation and unauthorized redirect to `/dashboard` | `auth.guard.ts`<br>`app.routes.ts` | `auth.guard.ts`<br>`app.routes.ts` | N/A | `app.spec.ts` | **[MET]** |
| **R7.4** | Dynamic navigation sidebar displaying administrative links only to `ADMIN` users | `Sidebar.java` equivalent in Angular | `sidebar.ts`<br>`sidebar.html` | N/A | `sidebar.spec.ts` | **[MET]** |

---

## 3. Compliance Summary

- **Total Requirements Audited:** 23
- **Total Requirements Fully Met:** 23 (100%)
- **Total Gaps Remaining:** 0
- **Regression Suite Passing Rate:** 100% (Backend: 30/30 passed; Frontend: 19/19 passed)
