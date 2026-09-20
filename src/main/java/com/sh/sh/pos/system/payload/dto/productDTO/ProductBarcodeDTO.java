package com.sh.sh.pos.system.payload.dto.productdto;

import lombok.Data;

@Data
public class ProductBarcodeDTO {

    private Long id;

    private Long productId;      // required on create
    private Long variantId;      // optional — targets a specific variant

    private String barcode;      // required on create
    private String source;      // INTERNAL | SUPPLIER | EAN13 | UPC | QR_CODE
    private String note;
    private Boolean primary;
    private Boolean active;

    // Read-side context (filled by the mapper)
    private String productName;
    private String variantName;
}
