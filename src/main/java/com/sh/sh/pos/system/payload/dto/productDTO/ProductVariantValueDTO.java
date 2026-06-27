package com.sh.sh.pos.system.payload.dto.productDTO;

import lombok.Data;

@Data
public class ProductVariantValueDTO {
     private Long id;

    private Long optionValueId;

    private String optionName;

    private String value;
}
