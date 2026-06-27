package com.sh.sh.pos.system.controller.priceController;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.service.priceService.PriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PriceController {
     private final PriceService priceService;

    @GetMapping("/resolve")
    public ResponseEntity<BigDecimal> resolvePrice(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam Long customerId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(defaultValue = "1") Integer quantity) {

        return ResponseEntity.ok(
                priceService.resolvePrice(
                        productId,
                        variantId,
                        customerId,
                        branchId,
                        quantity
                )
        );
    }
}
