package com.sh.sh.pos.system.payload.dto.productdto;

import lombok.Data;

@Data
public class ProductVariantValueDTO {
     private Long id;

    private Long optionValueId;

    private String optionName;

    private String value;
}
