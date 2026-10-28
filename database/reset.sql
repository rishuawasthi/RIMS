-- =============================================================================
-- RETAIL INVENTORY MANAGEMENT SYSTEM (RIMS) - PRG_RIMS_001
-- Destructive Reset Script - DROPS ALL TABLES AND DATA
-- USE WITH CAUTION: This will permanently delete all stored data.
-- =============================================================================

DROP TABLE IF EXISTS stock_movement CASCADE;
DROP TABLE IF EXISTS order_item CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS purchase_order CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS supplier CASCADE;
DROP TABLE IF EXISTS warehouse CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
