package com.sh.sh.pos.system.domain;

public enum MovementType {
    PURCHASE,    // stock-in (from supplier)
    SALE,        // stock-out (to customer)
    RETURN,      // returned sales
    DAMAGE,      // spoiled/damaged stock decrement
    TRANSFER_IN, // transfer from another branch
    TRANSFER_OUT,// transfer to another branch
    ADJUSTMENT,   // manual inventory correction
    RECEIPT,
    PURCHASE_RETURN,
    SALES_RETURN,
    OPENING_STOCK
}
