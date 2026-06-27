package com.sh.sh.pos.system.service.priceService;

import java.math.BigDecimal;

public interface PriceService {
    BigDecimal resolvePrice(
        Long productId,
        Long variantId,
        Long customerId,
        Long branchId,
        Integer quantity
    );
}
