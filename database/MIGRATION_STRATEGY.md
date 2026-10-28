# RIMS Database Migration & Data Preservation Strategy

## 1. Principles of Data Preservation
1. **Zero Destructive DDL in Normal Execution:** All production/development migrations must use non-destructive statements (`CREATE TABLE IF NOT EXISTS`, `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`).
2. **Strict Separation of Reset Scripts:** Destructive reset scripts (`DROP TABLE ... CASCADE`) are isolated exclusively in `database/reset.sql` and must NEVER be executed during container startup, CI/CD pipelines, or deployment.
3. **Idempotent Seeding:** All seed scripts use `ON CONFLICT (...) DO NOTHING` or `ON CONFLICT (...) DO UPDATE` ensuring re-running seeds does not wipe or corrupt live transactional records.
4. **JPA Entity Synchronization:** The JPA entities declare `@Table`, `@Column`, `@JoinColumn`, and `@Check` constraints matching the physical database schema.

## 2. Safe Migration Workflow
- **Development & Staging:** Schema updates can be applied using `database/schema.sql` (which is idempotent and non-destructive) followed by `database/seed.sql`.
- **Production Upgrades:** Incremental migration scripts should be run sequentially using versioned scripts (e.g., `V1__init.sql`, `V2__add_index.sql`) or tools such as Flyway or Liquibase.
- **Teardown / Hard Reset:** Only when explicitly requested and approved in local test environments:
  ```bash
  psql -U postgres -d hcl -f database/reset.sql
  psql -U postgres -d hcl -f database/schema.sql
  psql -U postgres -d hcl -f database/seed.sql
  ```

## 3. Schema Inconsistency Audit & Resolutions
- **Supplier Name Length:** Increased from `VARCHAR(100)` to `VARCHAR(150)` to align with JPA `@Size(max = 150)`.
- **Customer Name & Email:** Increased to `VARCHAR(150)` and made `customer_email` nullable in `orders` table to align with JPA optional customer email.
- **Order Item Warehouse:** Made `warehouse_id` nullable in `order_item` table so unallocated order lines in draft orders can exist before warehouse assignment.
- **Contact Numbers:** Standardized to 10-15 digit string patterns without hyphens to pass Jakarta validation constraints.
