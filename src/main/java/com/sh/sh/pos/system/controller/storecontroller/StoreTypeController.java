package com.sh.sh.pos.system.controller.storecontroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.storedto.StoreTypeDTO;
import com.sh.sh.pos.system.service.storeservice.StoreTypeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/store-types")
@RequiredArgsConstructor
@Validated
public class StoreTypeController {
    private final StoreTypeService storeTypeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StoreTypeDTO> createStoreType(
            @Valid @RequestBody StoreTypeDTO storeTypeDTO) throws UserException {

        StoreTypeDTO created = storeTypeService.createStoreType(storeTypeDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // =========================================================
    // GET ALL STORE TYPES
    // GET /api/store-types
    // SUPER ADMIN ONLY
    // Includes inactive types
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<StoreTypeDTO>> getAllStoreTypes() {

        return ResponseEntity.ok(
                storeTypeService.getAllStoreTypes());
    }

    // =========================================================
    // GET ACTIVE STORE TYPES
    // GET /api/store-types/active
    // PUBLIC: for registration / onboarding dropdown
    // =========================================================

    @GetMapping("/active")
    public ResponseEntity<List<StoreTypeDTO>> getActiveStoreTypes() {

        return ResponseEntity.ok(
                storeTypeService.getActiveStoreTypes());
    }

    // =========================================================
    // GET STORE TYPE BY ID
    // GET /api/store-types/{id}
    // SUPER ADMIN ONLY
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StoreTypeDTO> getStoreTypeById(
            @PathVariable @Positive Long id) throws UserException {

        return ResponseEntity.ok(
                storeTypeService.getStoreTypeById(id));
    }

    // =========================================================
    // UPDATE STORE TYPE
    // PUT /api/store-types/{id}
    // SUPER ADMIN ONLY
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StoreTypeDTO> updateStoreType(
            @PathVariable @Positive Long id,
            @Valid @RequestBody StoreTypeDTO storeTypeDTO) throws UserException {

        StoreTypeDTO updated = storeTypeService.updateStoreType(id, storeTypeDTO);

        return ResponseEntity.ok(updated);
    }

    // =========================================================
    // DEACTIVATE STORE TYPE
    // PATCH /api/store-types/{id}/deactivate
    // SUPER ADMIN ONLY
    // =========================================================

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StoreTypeDTO> deactivateStoreType(
            @PathVariable @Positive Long id) throws UserException {

        return ResponseEntity.ok(
                storeTypeService.deactivateStoreType(id));
    }

    // =========================================================
    // ACTIVATE STORE TYPE
    // PATCH /api/store-types/{id}/activate
    // SUPER ADMIN ONLY
    // =========================================================

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StoreTypeDTO> activateStoreType(
            @PathVariable @Positive Long id) throws UserException {

        return ResponseEntity.ok(
                storeTypeService.activateStoreType(id));
    }

}
