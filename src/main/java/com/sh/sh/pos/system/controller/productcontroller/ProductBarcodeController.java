package com.sh.sh.pos.system.controller.productcontroller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.productdto.BarcodeLookupDTO;
import com.sh.sh.pos.system.payload.dto.productdto.ProductBarcodeDTO;
import com.sh.sh.pos.system.service.productservice.ProductBarcodeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-barcodes")
@RequiredArgsConstructor
public class ProductBarcodeController {

    private final ProductBarcodeService barcodeService;

    @PostMapping
    public ResponseEntity<ProductBarcodeDTO> create(@RequestBody ProductBarcodeDTO dto) {
        return ResponseEntity.ok(barcodeService.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductBarcodeDTO> update(
            @PathVariable Long id,
            @RequestBody ProductBarcodeDTO dto) {
        return ResponseEntity.ok(barcodeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        barcodeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductBarcodeDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(barcodeService.getByProduct(productId));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<ProductBarcodeDTO>> getByVariant(@PathVariable Long variantId) {
        return ResponseEntity.ok(barcodeService.getByVariant(variantId));
    }

    /** The POS register endpoint: scan → item + price. */
    @GetMapping("/lookup/{barcode}")
    public ResponseEntity<BarcodeLookupDTO> lookup(@PathVariable String barcode) {
        return ResponseEntity.ok(barcodeService.lookup(barcode));
    }
}
