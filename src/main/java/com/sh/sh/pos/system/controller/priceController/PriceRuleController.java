package com.sh.sh.pos.system.controller.priceController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.priceDTO.PriceRuleDTO;
import com.sh.sh.pos.system.service.priceService.PriceRuleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/price-rules")
@RequiredArgsConstructor
public class PriceRuleController {
    private final PriceRuleService priceRuleService;

    @PostMapping
    public ResponseEntity<PriceRuleDTO> create(@RequestBody PriceRuleDTO dto) {
        return ResponseEntity.ok(priceRuleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceRuleDTO> update(
            @PathVariable Long id,
            @RequestBody PriceRuleDTO dto) {
        return ResponseEntity.ok(priceRuleService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceRuleDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(priceRuleService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<PriceRuleDTO>> getAll() {
        return ResponseEntity.ok(priceRuleService.getAll());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PriceRuleDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(priceRuleService.getByProduct(productId));
    }

    @GetMapping("/price-group/{priceGroupId}")
    public ResponseEntity<List<PriceRuleDTO>> getByPriceGroup(@PathVariable Long priceGroupId) {
        return ResponseEntity.ok(priceRuleService.getByPriceGroup(priceGroupId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        priceRuleService.delete(id);
        return ResponseEntity.ok("Price rule deleted successfully");
    }
}
