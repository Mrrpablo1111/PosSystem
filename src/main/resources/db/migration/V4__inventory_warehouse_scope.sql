-- Inventory: branch-level -> warehouse(+variant)-level scope

-- 1. Add the new columns + FKs (must come BEFORE anything references them)
ALTER TABLE inventories ADD COLUMN warehouse_id BIGINT NULL;
ALTER TABLE inventories ADD COLUMN product_variant_id BIGINT NULL;

ALTER TABLE inventories ADD CONSTRAINT fk_inventory_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouses (id);
ALTER TABLE inventories ADD CONSTRAINT fk_inventory_variant
    FOREIGN KEY (product_variant_id) REFERENCES product_variants (id);

-- 2. Backfill existing rows to each branch's default warehouse
UPDATE inventories inv
JOIN warehouses w ON w.branch_id = inv.branch_id AND w.is_default = TRUE
SET inv.warehouse_id = w.id
WHERE inv.warehouse_id IS NULL;

-- 3. Swap the unique constraint (Hibernate-generated name from SHOW INDEX)
ALTER TABLE inventories DROP INDEX UKp5ux0ijhbxmbxcp04ae1t4kdx;

ALTER TABLE inventories
    ADD CONSTRAINT uk_inventory_warehouse_product_variant
        UNIQUE (warehouse_id, product_id, product_variant_id);

-- 4. Safety defaults
ALTER TABLE inventories MODIFY COLUMN reorder_level INT NOT NULL DEFAULT 0;
ALTER TABLE inventories MODIFY COLUMN reserved_quantity INT NOT NULL DEFAULT 0;