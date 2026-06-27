package com.sh.sh.pos.system.payload.dto.productDTO;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductDetailsDTO {
    private Long id;

    private String name;

    private String sku;

    private BigDecimal costPrice;

    private BigDecimal sellingPrice;

    private String barcode;

    private String brand;

    private String image;

    private List<ProductOptionDTO> options;

    private List<ProductVariantDTO> variants;
}
