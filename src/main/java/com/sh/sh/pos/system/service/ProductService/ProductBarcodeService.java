package com.sh.sh.pos.system.service.productservice;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.productdto.BarcodeLookupDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBarcodeDTO;

public interface ProductBarcodeService {

    ProductBarcodeDTO create(ProductBarcodeDTO dto);

    ProductBarcodeDTO update(Long id, ProductBarcodeDTO dto);

    void delete(Long id);

    List<ProductBarcodeDTO> getByProduct(Long productId);

    List<ProductBarcodeDTO> getByVariant(Long variantId);

    /** POS register resolution: any barcode → the sellable item + price. */
    BarcodeLookupDTO lookup(String barcode);
}
