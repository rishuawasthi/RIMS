-- =============================================================================
-- RETAIL INVENTORY MANAGEMENT SYSTEM (RIMS) - PRG_RIMS_001
-- Client: XYZ Retail Corporation
-- Database Schema Script (PostgreSQL 15+)
-- Non-destructive initialization: preserves existing data and tables.
-- For destructive reset, see database/reset.sql.
-- =============================================================================

-- 1. App Users & Role-Based Access Control
CREATE TABLE IF NOT EXISTS app_user (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'STORE_STAFF',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_username ON app_user(LOWER(username));
CREATE INDEX IF NOT EXISTS idx_user_email ON app_user(LOWER(email));

-- 2. Product Master Catalog (Apparel, Home Goods, Electronics)
CREATE TABLE IF NOT EXISTS product (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    product_price NUMERIC(12, 2) NOT NULL CHECK (product_price > 0),
    product_category VARCHAR(50) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    brand VARCHAR(100),
    description VARCHAR(500),
    image_url VARCHAR(255),
    unit VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_sku ON product(LOWER(sku));
CREATE INDEX IF NOT EXISTS idx_product_category ON product(LOWER(product_category));

-- 3. Warehouses & Retail Stores Network (500+ Store Topology)
CREATE TABLE IF NOT EXISTS warehouse (
    warehouse_id SERIAL PRIMARY KEY,
    warehouse_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_name VARCHAR(100) NOT NULL,
    warehouse_location VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(20) NOT NULL,
    manager_name VARCHAR(100),
    contact_number VARCHAR(20),
    email VARCHAR(100),
    capacity INT CHECK (capacity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_warehouse_code ON warehouse(LOWER(warehouse_code));

-- 4. Multi-Location Real-time Inventory Levels
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
    warehouse_id INT NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE RESTRICT,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reorder_level INT NOT NULL DEFAULT 10 CHECK (reorder_level >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_reserved_lte_qty CHECK (reserved_quantity <= quantity)
);

CREATE INDEX IF NOT EXISTS idx_inv_product ON inventory(product_id);
CREATE INDEX IF NOT EXISTS idx_inv_warehouse ON inventory(warehouse_id);

-- 5. Supplier Management
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id SERIAL PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(20) NOT NULL,
    tax_id VARCHAR(50),
    payment_terms VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_supplier_code ON supplier(LOWER(supplier_code));

-- 6. Purchase Orders (Procurement & Restocking)
CREATE TABLE IF NOT EXISTS purchase_order (
    po_id SERIAL PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id INT NOT NULL REFERENCES supplier(supplier_id) ON DELETE RESTRICT,
    product_id INT NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
    warehouse_id INT NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    received_quantity INT NOT NULL DEFAULT 0 CHECK (received_quantity >= 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price > 0),
    total_amount NUMERIC(12, 2) NOT NULL,
    expected_delivery_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_po_number ON purchase_order(LOWER(po_number));

-- 7. Sales Orders (Customer Order Management)
CREATE TABLE IF NOT EXISTS orders (
    order_id SERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    customer_email VARCHAR(150),
    customer_phone VARCHAR(20),
    shipping_address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pincode VARCHAR(20) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_number ON orders(LOWER(order_number));

-- 8. Customer Order Line Items
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
    warehouse_id INT REFERENCES warehouse(warehouse_id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price > 0),
    allocated_quantity INT NOT NULL DEFAULT 0 CHECK (allocated_quantity >= 0),
    subtotal NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item(order_id);

-- 9. Real-time Stock Movement Ledger & Audit Logs
CREATE TABLE IF NOT EXISTS stock_movement (
    movement_id BIGSERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES product(product_id) ON DELETE RESTRICT,
    warehouse_id INT NOT NULL REFERENCES warehouse(warehouse_id) ON DELETE RESTRICT,
    movement_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    reference_number VARCHAR(100),
    reason VARCHAR(255),
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movement_product ON stock_movement(product_id);
CREATE INDEX IF NOT EXISTS idx_movement_warehouse ON stock_movement(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_movement_created ON stock_movement(created_at DESC);
