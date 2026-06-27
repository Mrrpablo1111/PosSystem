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

import com.sh.sh.pos.system.payload.dto.priceDTO.PriceGroupDTO;
import com.sh.sh.pos.system.service.priceService.PriceGroupService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/price-groups")
@RequiredArgsConstructor
public class PriceGroupController {
    private final PriceGroupService priceGroupService;

    @PostMapping
    public ResponseEntity<PriceGroupDTO> create(@RequestBody PriceGroupDTO dto) {
        return ResponseEntity.ok(priceGroupService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceGroupDTO> update(
            @PathVariable Long id,
            @RequestBody PriceGroupDTO dto) {
        return ResponseEntity.ok(priceGroupService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceGroupDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(priceGroupService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<PriceGroupDTO>> getAll() {
        return ResponseEntity.ok(priceGroupService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        priceGroupService.delete(id);
        return ResponseEntity.ok("Price group deleted successfully");
    }
}
