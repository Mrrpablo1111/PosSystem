package com.sh.sh.pos.system.repository.productbarcoderepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sh.sh.pos.system.model.products.ProductBarcode;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcode, Long> {

    Optional<ProductBarcode> findByBarcodeAndActiveTrue(String barcode);

    boolean existsByBarcode(String barcode);

    List<ProductBarcode> findByProductIdOrderByPrimaryDescCreatedAtAsc(Long productId);

    List<ProductBarcode> findByVariantId(Long variantId);

    List<ProductBarcode> findByProductIdAndVariantId(Long productId, Long variantId);
}
