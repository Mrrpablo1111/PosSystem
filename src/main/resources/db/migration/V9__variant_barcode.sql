ALTER TABLE product_variants ADD COLUMN barcode VARCHAR(13) NULL;
ALTER TABLE product_variants ADD CONSTRAINT uk_variant_barcode UNIQUE (barcode);