package com.sh.sh.pos.system.service.serviceimpl.warehousesserviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.warehousesmapper.WarehousesMapper;
import com.sh.sh.pos.system.model.branch.Branch;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.warehousesdto.WarehouseDTO;
import com.sh.sh.pos.system.repository.branchrepository.BranchRepository;
import com.sh.sh.pos.system.repository.warehousesrepository.WarehouseRepository;
import com.sh.sh.pos.system.service.warehousesservice.WarehouseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public WarehouseDTO create(WarehouseDTO dto) {

        if (warehouseRepository.existsByCode(dto.getCode())) {
            throw new UserException("Warehouse code already exists: " + dto.getCode());
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Branch not found with id: " + dto.getBranchId()));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            unsetCurrentDefault(branch.getId(), null);
        }

        Warehouse warehouse = Warehouse.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .isDefault(dto.getIsDefault())
                .branch(branch)
                .active(true)
                .build();

        return WarehousesMapper.toDTO(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseDTO update(Long id, WarehouseDTO dto) {

        Warehouse warehouse = findWarehouseOrThrow(id);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            unsetCurrentDefault(warehouse.getBranch().getId(), warehouse.getId());
        }

        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setDescription(dto.getDescription());
        warehouse.setIsDefault(dto.getIsDefault());

        return WarehousesMapper.toDTO(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getById(Long id) {
        return WarehousesMapper.toDTO(findWarehouseOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getAll() {
        return WarehousesMapper.toDTOList(warehouseRepository.findByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getByBranch(Long branchId) {
        return WarehousesMapper.toDTOList(
                warehouseRepository.findByBranchIdAndActiveTrue(branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getDefaultWarehouse(Long branchId) {
        return WarehousesMapper.toDTO(
                warehouseRepository.findByBranchIdAndIsDefaultTrue(branchId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Default warehouse not found for branch id: " + branchId)));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse not found with id: " + id));
    }

    /**
     * Unsets the current default warehouse for the branch, if one exists and it
     * isn't the warehouse currently being modified ({@code excludeId}).
     */
    private void unsetCurrentDefault(Long branchId, Long excludeId) {
        warehouseRepository.findByBranchIdAndIsDefaultTrue(branchId)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    warehouseRepository.save(existing);
                });
    }
}