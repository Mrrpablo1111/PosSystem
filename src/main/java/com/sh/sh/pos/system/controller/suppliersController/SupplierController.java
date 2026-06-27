package com.sh.sh.pos.system.controller.suppliersController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.suppliersDTO.SupplierDTO;
import com.sh.sh.pos.system.service.suppliersService.SupplierService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public SupplierDTO create(@RequestBody SupplierDTO dto) {
        return supplierService.create(dto);
    }

    @PutMapping("/{id}")
    public SupplierDTO update(@PathVariable Long id,
                              @RequestBody SupplierDTO dto) {
        return supplierService.update(id, dto);
    }

    @GetMapping("/{id}")
    public SupplierDTO getById(@PathVariable Long id) {
        return supplierService.getById(id);
    }

    @GetMapping("/store/{storeId}")
    public List<SupplierDTO> getByStore(@PathVariable Long storeId) {
        return supplierService.getByStore(storeId);
    }

    @PutMapping("/{id}/inactive")
    public void inactive(@PathVariable Long id) {
        supplierService.inactive(id);
    }

    @PutMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        supplierService.activate(id);
    }
}