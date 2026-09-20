package com.sh.sh.pos.system.payload.dto.productdto;



import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

/**
 * What the POS register needs back from one scan:
 * which sellable item this is, and what to charge.
 */
@Data
@Builder
public class BarcodeLookupDTO {

    private String barcode;
    private String barcodeSource;   // INTERNAL | SUPPLIER | EAN13 | UPC | QR_CODE (null for legacy columns)
    private String resolvedFrom;    // PRODUCT_BARCODE | VARIANT | PRODUCT

    private Long productId;
    private String productName;

    private Long variantId;         // null for simple products
    private String variantName;

    private String sku;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private String image;

    /** Total on-hand quantity across all warehouses for this item. */
    private Integer currentStock;
}
