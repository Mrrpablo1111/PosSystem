package com.sh.sh.pos.system.payload.dto.productDTO;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductVariantDTO {
    private Long id;

    private Long productId;

    private String itemCode;
    private String name;
    private String barcode;

    private String sku;
    private BigDecimal costPrice;

    private BigDecimal sellingPrice;

    private Integer stock;

    private Boolean active;

    private List<ProductVariantValueDTO> values;
}
