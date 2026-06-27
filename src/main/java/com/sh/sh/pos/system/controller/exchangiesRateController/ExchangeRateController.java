package com.sh.sh.pos.system.controller.exchangiesRateController;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.payload.dto.exchangiesRateDTO.ExchangeRateDTO;
import com.sh.sh.pos.system.service.exchangiesRateService.ExchangeRateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    public ResponseEntity<ExchangeRateDTO> create(
            @RequestBody ExchangeRateDTO dto) {

        return ResponseEntity.ok(
                exchangeRateService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRateDTO>> getAll(
            @RequestParam(required = false) Boolean active) {

        return ResponseEntity.ok(
                exchangeRateService.getAll(active));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activate(
            @PathVariable Long id) {

        exchangeRateService.activate(id);

        return ResponseEntity.ok("Activated");
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id) {

        exchangeRateService.deactivate(id);

        return ResponseEntity.ok("Deactivated");
    }

    @GetMapping("/latest")
    public ResponseEntity<ExchangeRateDTO> latest(
            @RequestParam String from,
            @RequestParam String to) {

        return ResponseEntity.ok(
                exchangeRateService.getLatestRate(from, to));
    }
}