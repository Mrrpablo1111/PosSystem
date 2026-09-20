package com.sh.sh.pos.system.service.serviceimpl.inventoryserviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.service.inventoryservice.InventoryCalculationService;

@Service
public class InventoryCalculationServiceImpl implements InventoryCalculationService {

    @Override
    public int getAvailableQuantity(int onHandQuantity, int reservedQuantity) {
        return Math.max(0, onHandQuantity - reservedQuantity);
    }

    @Override
    public int getOnHandQuantity(int storedQuantity) {
        return storedQuantity;
    }

    @Override
    public BigDecimal getInventoryValue(int quantity, BigDecimal unitCost) {
        if (unitCost == null) return BigDecimal.ZERO;
        return unitCost.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getProfit(BigDecimal sellingPrice, BigDecimal costPrice) {
        BigDecimal sp = sellingPrice != null ? sellingPrice : BigDecimal.ZERO;
        BigDecimal cp = costPrice != null ? costPrice : BigDecimal.ZERO;
        return sp.subtract(cp).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getMargin(BigDecimal sellingPrice, BigDecimal costPrice) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = getProfit(sellingPrice, costPrice);
        return profit
                .divide(sellingPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isLowStock(int availableQuantity, int reorderLevel) {
        return availableQuantity <= reorderLevel;
    }

    @Override
    public boolean isOutOfStock(int availableQuantity) {
        return availableQuantity <= 0;
    }

    @Override
    public String getStockStatus(int availableQuantity, int reorderLevel) {
        if (isOutOfStock(availableQuantity)) return "OUT_OF_STOCK";
        if (isLowStock(availableQuantity, reorderLevel)) return "LOW";
        return "OK";
    }
}
