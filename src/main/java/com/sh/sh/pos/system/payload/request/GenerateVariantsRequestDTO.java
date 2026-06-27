package com.sh.sh.pos.system.payload.request;

import java.util.List;

import lombok.Data;

@Data
public class GenerateVariantsRequestDTO {
    private Long productId;

    private List<Long> optionIds;
}
