CREATE TABLE goods_receipts (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    grn_number          VARCHAR(30)  NOT NULL,
    purchase_order_id   BIGINT       NOT NULL,
    supplier_id         BIGINT       NOT NULL,
    warehouse_id        BIGINT       NOT NULL,
    branch_id           BIGINT       NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    received_date       DATE         NOT NULL,
    received_by         BIGINT       NULL,
    note                VARCHAR(500) NULL,
    created_at          DATETIME(6)  NOT NULL,
    updated_at          DATETIME(6)  NULL,

    CONSTRAINT uk_goods_receipt_grn_number UNIQUE (grn_number),
    CONSTRAINT fk_grn_purchase_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id),
    CONSTRAINT fk_grn_supplier       FOREIGN KEY (supplier_id)       REFERENCES suppliers (id),
    CONSTRAINT fk_grn_warehouse      FOREIGN KEY (warehouse_id)      REFERENCES warehouses (id),
    CONSTRAINT fk_grn_branch         FOREIGN KEY (branch_id)         REFERENCES branches (id),
    CONSTRAINT fk_grn_received_by    FOREIGN KEY (received_by)       REFERENCES users (id)
) ENGINE = InnoDB;

CREATE INDEX idx_grn_purchase_order ON goods_receipts (purchase_order_id);
CREATE INDEX idx_grn_status         ON goods_receipts (status);
CREATE INDEX idx_grn_received_date  ON goods_receipts (received_date);

CREATE TABLE goods_receipt_items (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_receipt_id         BIGINT         NOT NULL,
    purchase_order_item_id   BIGINT         NOT NULL,
    product_id               BIGINT         NOT NULL,
    product_variant_id       BIGINT         NULL,
    requested_quantity       INT            NOT NULL,
    received_quantity        INT            NOT NULL,
    accepted_quantity        INT            NOT NULL,
    damaged_quantity         INT            NOT NULL DEFAULT 0,
    rejected_quantity        INT            NOT NULL DEFAULT 0,
    unit_cost                DECIMAL(19,4)  NOT NULL,
    batch_number             VARCHAR(60)    NULL,
    expiry_date              DATE           NULL,
    manufacture_date         DATE           NULL,
    note                     VARCHAR(500)   NULL,

    CONSTRAINT fk_gri_goods_receipt FOREIGN KEY (goods_receipt_id)       REFERENCES goods_receipts (id),
    CONSTRAINT fk_gri_po_item       FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items (id),
    CONSTRAINT fk_gri_product       FOREIGN KEY (product_id)             REFERENCES products (id),
    CONSTRAINT fk_gri_variant       FOREIGN KEY (product_variant_id)     REFERENCES product_variants (id)
) ENGINE = InnoDB;

CREATE INDEX idx_gri_goods_receipt ON goods_receipt_items (goods_receipt_id);
CREATE INDEX idx_gri_po_item       ON goods_receipt_items (purchase_order_item_id);