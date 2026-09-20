package com.sh.sh.pos.system.service.inventoryservice;

import java.math.BigDecimal;

/**
 * Every inventory-related CALCULATION lives here and nowhere else —
 * no controller, entity, or other service should compute these inline.
 * Pure functions: no repository injections, no @Transactional, trivially
 * unit-testable with plain numbers.
 */
public interface InventoryCalculationService {

    /** available = onHand - reserved (never returns negative — clamps at 0) */
    int getAvailableQuantity(int onHandQuantity, int reservedQuantity);

    /** onHand is just the raw stored quantity — named explicitly so callers
     *  never confuse "on hand" with "available" in code that reads it later. */
    int getOnHandQuantity(int storedQuantity);

    BigDecimal getInventoryValue(int quantity, BigDecimal unitCost);

    BigDecimal getProfit(BigDecimal sellingPrice, BigDecimal costPrice);

    /** Returns a percentage, e.g. 33.33 for a $12 sell / $8 cost item. Returns
     *  BigDecimal.ZERO if sellingPrice is zero or null (never divides by zero). */
    BigDecimal getMargin(BigDecimal sellingPrice, BigDecimal costPrice);

    boolean isLowStock(int availableQuantity, int reorderLevel);

    boolean isOutOfStock(int availableQuantity);

    /** "OUT_OF_STOCK" | "LOW" | "OK" — single source of truth for stock-status
     *  badges across dashboard, reports, and product detail pages. */
    String getStockStatus(int availableQuantity, int reorderLevel);
}
