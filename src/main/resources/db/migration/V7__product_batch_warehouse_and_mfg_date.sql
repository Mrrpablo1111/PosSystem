ALTER TABLE product_batches ADD COLUMN warehouse_id BIGINT NULL AFTER branch_id;
ALTER TABLE product_batches ADD COLUMN manufacture_date DATE NULL AFTER expiry_date;

ALTER TABLE product_batches
    ADD CONSTRAINT fk_product_batch_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses (id);

UPDATE product_batches pb
JOIN warehouses w ON w.branch_id = pb.branch_id AND w.is_default = TRUE
SET pb.warehouse_id = w.id
WHERE pb.warehouse_id IS NULL;

ALTER TABLE product_batches MODIFY COLUMN warehouse_id BIGINT NOT NULL;

CREATE INDEX idx_product_batch_branch ON product_batches (branch_id);

ALTER TABLE product_batches DROP INDEX UK2tnhovdebfcy2v4ao8y89xbau;

ALTER TABLE product_batches
    ADD CONSTRAINT uk_batch_warehouse_product_lot
        UNIQUE (warehouse_id, product_id, batch_no);