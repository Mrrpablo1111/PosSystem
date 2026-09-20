package com.sh.sh.pos.system.domain;

/** Where a barcode comes from / what standard it follows. */
public enum BarcodeSource {
    INTERNAL,   // generated in-store (our "20"-prefixed EAN-13s)
    SUPPLIER,   // printed by the supplier on the packaging
    EAN13,      // standard retail EAN-13
    UPC,        // 12-digit UPC-A
    QR_CODE     // QR on the label
}
