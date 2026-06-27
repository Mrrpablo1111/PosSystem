package com.sh.sh.pos.system.payload.dto.productDTO;

import java.util.List;

import lombok.Data;

@Data
public class ProductOptionDTO {
    private Long id;

    private Long productId;

    private String name; // Size, Color, Material

     private List<ProductOptionValueDTO> values;
}
