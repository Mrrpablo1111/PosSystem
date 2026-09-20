CREATE TABLE product_barcodes (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id         BIGINT       NOT NULL,
    product_variant_id BIGINT       NULL,
    barcode            VARCHAR(64)  NOT NULL,
    source             VARCHAR(20)  NOT NULL DEFAULT 'INTERNAL',
    note               VARCHAR(120) NULL,
    is_primary         BIT(1)       NOT NULL DEFAULT b'0',
    active             BIT(1)       NOT NULL DEFAULT b'1',
    created_at         DATETIME(6)  NOT NULL,

    CONSTRAINT uk_product_barcode UNIQUE (barcode),
    CONSTRAINT fk_pb_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_pb_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants (id)
) ENGINE = InnoDB;

CREATE INDEX idx_pb_product ON product_barcodes (product_id);
CREATE INDEX idx_pb_variant ON product_barcodes (product_variant_id);
