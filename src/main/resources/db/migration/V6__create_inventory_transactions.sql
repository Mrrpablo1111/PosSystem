CREATE TABLE inventory_transactions (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id          BIGINT         NOT NULL,
    product_variant_id  BIGINT         NULL,
    warehouse_id        BIGINT         NOT NULL,
    quantity            INT            NOT NULL,
    unit_cost           DECIMAL(19,4)  NULL,
    reference_type      VARCHAR(30)    NOT NULL,
    reference_id        BIGINT         NULL,
    performed_by        BIGINT         NULL,
    note                VARCHAR(500)   NULL,
    created_at          DATETIME(6)    NOT NULL,

    CONSTRAINT fk_invtxn_product      FOREIGN KEY (product_id)         REFERENCES products (id),
    CONSTRAINT fk_invtxn_variant      FOREIGN KEY (product_variant_id) REFERENCES product_variants (id),
    CONSTRAINT fk_invtxn_warehouse    FOREIGN KEY (warehouse_id)       REFERENCES warehouses (id),
    CONSTRAINT fk_invtxn_performed_by FOREIGN KEY (performed_by)       REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_invtxn_product   ON inventory_transactions (product_id);
CREATE INDEX idx_invtxn_warehouse ON inventory_transactions (warehouse_id);
CREATE INDEX idx_invtxn_reference ON inventory_transactions (reference_type, reference_id);
CREATE INDEX idx_invtxn_created   ON inventory_transactions (created_at);