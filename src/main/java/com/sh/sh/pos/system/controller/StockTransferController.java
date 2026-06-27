package com.sh.sh.pos.system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.domain.StockTransferStatus;
import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.stockDTO.StockTransferDTO;
import com.sh.sh.pos.system.service.UserService;
import com.sh.sh.pos.system.service.stockService.StockTransferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {
     private final StockTransferService stockTransferService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<StockTransferDTO> create(
            @RequestBody StockTransferDTO dto,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.create(dto, user));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<StockTransferDTO> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.approve(id, user));
    }

    @PatchMapping("/{id}/picking")
    public ResponseEntity<StockTransferDTO> markPicking(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.markPicking(id, user));
    }

    @PatchMapping("/{id}/packed")
    public ResponseEntity<StockTransferDTO> markPacked(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.markPacked(id, user));
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<StockTransferDTO> ship(
            @PathVariable Long id,
            @RequestBody StockTransferDTO dto,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.ship(id, dto, user));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<StockTransferDTO> receive(
            @PathVariable Long id,
            @RequestBody StockTransferDTO dto,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        return ResponseEntity.ok(stockTransferService.receive(id, dto, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<StockTransferDTO>> getAll() {
        return ResponseEntity.ok(stockTransferService.getAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<StockTransferDTO>> getByStatus(
            @PathVariable StockTransferStatus status) {

        return ResponseEntity.ok(stockTransferService.getByStatus(status));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.getUserFromJwtToken(jwt);
        stockTransferService.cancel(id, user);

        return ResponseEntity.noContent().build();
    }
}
