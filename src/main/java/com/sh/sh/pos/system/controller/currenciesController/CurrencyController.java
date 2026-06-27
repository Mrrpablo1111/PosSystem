package com.sh.sh.pos.system.controller.currenciesController;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sh.sh.pos.system.payload.dto.currenciesDTO.CurrencyDTO;
import com.sh.sh.pos.system.payload.request.CurrencyRequestDTO;
import com.sh.sh.pos.system.service.currenciesService.CurrencyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService service;

    /** GET /api/currencies — all currencies */
    @GetMapping
    public ResponseEntity<List<CurrencyDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** GET /api/currencies/active — only active */
    @GetMapping("/active")
    public ResponseEntity<List<CurrencyDTO>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    /** GET /api/currencies/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<CurrencyDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /** GET /api/currencies/code/{code} e.g. /code/USD */
    @GetMapping("/code/{code}")
    public ResponseEntity<CurrencyDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getByCode(code));
    }

    /** POST /api/currencies */
    @PostMapping
    public ResponseEntity<CurrencyDTO> create(@Valid @RequestBody CurrencyRequestDTO req) {
        return ResponseEntity.ok(service.create(req));
    }

    /** PUT /api/currencies/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<CurrencyDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequestDTO req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    /** PATCH /api/currencies/{id}/activate */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CurrencyDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.setActive(id, true));
    }

    /** PATCH /api/currencies/{id}/deactivate */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CurrencyDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.setActive(id, false));
    }

    /** DELETE /api/currencies/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}