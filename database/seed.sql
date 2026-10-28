-- =============================================================================
-- RETAIL INVENTORY MANAGEMENT SYSTEM (RIMS) - PRG_RIMS_001
-- Client: XYZ Retail Corporation
-- Realistic Seed Data Script
-- =============================================================================

-- Default Seed Users
-- Password for all seed users is: Password123!
-- Verified Spring Security BCrypt Hash: $2a$10$wN3k9p8Z4K.u7XwE5mG1g.n5l0tF0/qjY7pQ.p1v0rR1x0k0Z4m2W
INSERT INTO app_user (username, password, full_name, email, phone, role, status, failed_login_attempts, created_at, updated_at)
VALUES
('admin', '$2a$10$wN3k9p8Z4K.u7XwE5mG1g.n5l0tF0/qjY7pQ.p1v0rR1x0k0Z4m2W', 'System Administrator', 'admin@xyzretail.com', '9876543210', 'ADMIN', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('manager.north', '$2a$10$wN3k9p8Z4K.u7XwE5mG1g.n5l0tF0/qjY7pQ.p1v0rR1x0k0Z4m2W', 'Rajesh Sharma', 'rajesh.sharma@xyzretail.com', '9876543211', 'STORE_MANAGER', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('staff.mumbai', '$2a$10$wN3k9p8Z4K.u7XwE5mG1g.n5l0tF0/qjY7pQ.p1v0rR1x0k0Z4m2W', 'Pooja Verma', 'pooja.verma@xyzretail.com', '9876543212', 'WAREHOUSE_STAFF', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('auditor.lead', '$2a$10$wN3k9p8Z4K.u7XwE5mG1g.n5l0tF0/qjY7pQ.p1v0rR1x0k0Z4m2W', 'Sunil Narang', 'sunil.narang@xyzretail.com', '9876543213', 'AUDITOR', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
    status = EXCLUDED.status,
    phone = EXCLUDED.phone,
    failed_login_attempts = 0,
    updated_at = CURRENT_TIMESTAMP;

-- Product Catalog across Apparel, Home Goods, Electronics
INSERT INTO product (product_name, product_price, product_category, sku, brand, description, unit, status)
VALUES
('Ultra-Slim Business Laptop 14"', 64999.00, 'Electronics', 'ELEC-LP-001', 'TechPro', '14-inch Full HD display, Intel Core i7, 16GB RAM, 512GB SSD', 'pcs', 'ACTIVE'),
('Wireless Noise-Canceling Headphones', 8999.00, 'Electronics', 'ELEC-HP-002', 'AcousticMax', 'Over-ear Bluetooth 5.3 headphones with active ANC and 40h battery', 'pcs', 'ACTIVE'),
('Smart Fitness Tracker Band 6', 2499.00, 'Electronics', 'ELEC-FT-003', 'PulseFit', 'Color AMOLED display, heart rate, SpO2 sensor, 5ATM water resistant', 'pcs', 'ACTIVE'),
('4K Ultra HD Smart TV 55"', 42999.00, 'Electronics', 'ELEC-TV-004', 'VisionPlus', '55-inch HDR10+, Dolby Audio, Google TV OS, voice remote', 'pcs', 'ACTIVE'),
('Classic Cotton Oxford Shirt - Blue', 1599.00, 'Apparel', 'APP-TS-010', 'UrbanClassic', '100% breathable organic cotton, wrinkle-free, slim tailored fit', 'pcs', 'ACTIVE'),
('Slim Fit Denim Jeans - Indigo', 2299.00, 'Apparel', 'APP-JN-011', 'DenimCo', 'Comfort stretch premium denim, stone washed, durable rivets', 'pcs', 'ACTIVE'),
('All-Weather Softshell Jacket', 3499.00, 'Apparel', 'APP-JK-012', 'Outbound', 'Windproof and water-resistant athletic outerwear jacket with fleece lining', 'pcs', 'ACTIVE'),
('Premium Percale Bed Linen Set', 2899.00, 'Home Goods', 'HOME-BL-101', 'CozyHaven', '400 thread-count combed cotton sheet set, includes 2 pillowcases', 'set', 'ACTIVE'),
('Stainless Steel Cookware Set 5pc', 4999.00, 'Home Goods', 'HOME-CW-102', 'ChefCraft', 'Tri-ply clad heavy gauge stainless steel pots and pans with tempered glass lids', 'set', 'ACTIVE'),
('Aromatherapy Ultrasonic Diffuser', 1499.00, 'Home Goods', 'HOME-DF-103', 'AuraMist', '500ml capacity, 7 LED color lights, auto shut-off, whisper quiet', 'pcs', 'ACTIVE')
ON CONFLICT (sku) DO NOTHING;

-- Warehouses & Distribution Hubs
INSERT INTO warehouse (warehouse_code, warehouse_name, warehouse_location, address, city, state, pincode, manager_name, contact_number, email, capacity, status)
VALUES
('WH-MUM-01', 'Western Central DC', 'Bhiwandi Logistics Park', 'Bldg 4A, Bhiwandi Highway Zone', 'Mumbai', 'Maharashtra', '421302', 'Vikram Deshmukh', '02228491001', 'wh.mumbai@xyzretail.com', 250000, 'ACTIVE'),
('WH-DEL-02', 'Northern Hub DC', 'NH-48 Industrial Corridor', 'Plot 18, Bilaspur Industrial Area', 'Gurugram', 'Haryana', '122413', 'Anil Kashyap', '01244921002', 'wh.delhi@xyzretail.com', 300000, 'ACTIVE'),
('WH-BLR-03', 'Southern Fulfilment Center', 'Hosur Road Logistics Hub', 'Sector 3, Electronic City Phase 2', 'Bengaluru', 'Karnataka', '560100', 'Suresh Nair', '08068191003', 'wh.bangalore@xyzretail.com', 200000, 'ACTIVE'),
('STR-501-MUM', 'XYZ Store #501 - Bandra Flagship', 'Linking Road Mall', '102 Linking Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 'Sneha Kapoor', '02267123501', 'store501@xyzretail.com', 15000, 'ACTIVE'),
('STR-502-DEL', 'XYZ Store #502 - Connaught Place', 'CP Outer Circle', 'Block B, Connaught Place', 'New Delhi', 'Delhi', '110001', 'Manish Gupta', '01123719502', 'store502@xyzretail.com', 18000, 'ACTIVE')
ON CONFLICT (warehouse_code) DO NOTHING;

-- Real-time Stock Levels (Inventory)
INSERT INTO inventory (product_id, warehouse_id, quantity, reorder_level, reserved_quantity, status)
VALUES
(1, 1, 120, 25, 10, 'ACTIVE'),
(1, 2, 85, 20, 5, 'ACTIVE'),
(1, 4, 12, 15, 0, 'ACTIVE'),
(2, 1, 350, 50, 20, 'ACTIVE'),
(2, 3, 210, 40, 15, 'ACTIVE'),
(3, 1, 500, 100, 45, 'ACTIVE'),
(3, 4, 40, 20, 2, 'ACTIVE'),
(4, 2, 60, 15, 8, 'ACTIVE'),
(5, 1, 800, 150, 50, 'ACTIVE'),
(5, 4, 95, 30, 10, 'ACTIVE'),
(5, 5, 80, 25, 0, 'ACTIVE'),
(6, 1, 450, 80, 20, 'ACTIVE'),
(6, 5, 60, 20, 5, 'ACTIVE'),
(7, 2, 180, 40, 10, 'ACTIVE'),
(8, 1, 250, 40, 15, 'ACTIVE'),
(8, 3, 140, 30, 5, 'ACTIVE'),
(9, 1, 160, 25, 12, 'ACTIVE'),
(10, 3, 300, 50, 10, 'ACTIVE')
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

-- Suppliers (Vendors)
INSERT INTO supplier (supplier_code, supplier_name, contact_person, email, phone, address, city, state, pincode, tax_id, payment_terms, status)
VALUES
('SUP-TECH-01', 'Apex Electronics Global Ltd', 'David Chen', 'orders@apexelectronics.com', '9811223344', 'Tower B, Cyber City', 'Gurugram', 'Haryana', '122002', '06AAACA1234A1Z5', 'Net 30', 'ACTIVE'),
('SUP-TEXT-02', 'Heritage Textiles & Apparel Ltd', 'Arjun Singhania', 'contact@heritagetextiles.in', '9822334455', 'Textile Hub, Ring Road', 'Surat', 'Gujarat', '395002', '24AAACH5678B1Z2', 'Net 45', 'ACTIVE'),
('SUP-HOME-03', 'Living Craft Home Industries', 'Kavita Iyer', 'sales@livingcrafthome.com', '9833445566', 'Industrial Area Phase 1', 'Peenya, Bengaluru', 'Karnataka', '560058', '29AAACL9012C1Z9', 'Net 30', 'ACTIVE')
ON CONFLICT (supplier_code) DO NOTHING;

-- Purchase Orders (Restocking & Procurement)
INSERT INTO purchase_order (po_number, supplier_id, product_id, warehouse_id, quantity, received_quantity, unit_price, total_amount, expected_delivery_date, status, notes)
VALUES
('PO-2026-001', 1, 1, 1, 100, 100, 52000.00, 5200000.00, '2026-11-05', 'RECEIVED', 'Procurement for Q4 tech festive demand'),
('PO-2026-002', 2, 5, 1, 500, 0, 1100.00, 550000.00, '2026-11-12', 'PLACED', 'Restocking Oxford shirts for Mumbai DC'),
('PO-2026-003', 3, 8, 3, 200, 0, 2100.00, 420000.00, '2026-11-15', 'PLACED', 'Winter linen replenishment for Bangalore DC')
ON CONFLICT (po_number) DO NOTHING;

-- Customer Sales Orders
INSERT INTO orders (order_number, customer_name, customer_email, customer_phone, shipping_address, city, state, pincode, total_amount, status, notes)
VALUES
('ORD-2026-1001', 'Amitabh Trivedi', 'amitabh.t@gmail.com', '9988776655', 'Flat 402, Sea Green Apts, Juhu', 'Mumbai', 'Maharashtra', '400049', 66598.00, 'CONFIRMED', 'Urgent weekend delivery requested'),
('ORD-2026-1002', 'Deepika Menon', 'deepika.m@outlook.com', '9871122334', '124, 4th Cross, Indiranagar', 'Bengaluru', 'Karnataka', '560038', 11498.00, 'PROCESSING', 'Deliver to reception during office hours')
ON CONFLICT (order_number) DO NOTHING;

-- Customer Order Items
INSERT INTO order_item (order_id, product_id, warehouse_id, quantity, unit_price, allocated_quantity, subtotal, status)
VALUES
(1, 1, 1, 1, 64999.00, 1, 64999.00, 'ALLOCATED'),
(1, 5, 1, 1, 1599.00, 1, 1599.00, 'ALLOCATED'),
(2, 2, 3, 1, 8999.00, 1, 8999.00, 'ALLOCATED'),
(2, 3, 3, 1, 2499.00, 1, 2499.00, 'ALLOCATED')
ON CONFLICT DO NOTHING;

-- Initial Stock Movement Ledger Records
INSERT INTO stock_movement (product_id, warehouse_id, movement_type, quantity, reference_number, reason, performed_by, created_at)
VALUES
(1, 1, 'INBOUND_PO', 100, 'PO-2026-001', 'Initial purchase order stock received', 'wh.staff', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(1, 4, 'WAREHOUSE_TRANSFER', 15, 'TRF-MUM-501', 'Inter-location replenishment Mumbai DC to Store 501', 'wh.manager', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(1, 1, 'OUTBOUND_SALE', 1, 'ORD-2026-1001', 'Customer order dispatch allocation', 'system', CURRENT_TIMESTAMP - INTERVAL '1 day');
