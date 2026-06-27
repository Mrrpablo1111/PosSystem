package com.sh.sh.pos.system.service.serviceImpl.warehousesServiceImpl;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.mapper.warehousesMapper.WarehousesMapper;
import com.sh.sh.pos.system.model.Branch;
import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.warehousesDTO.WarehouseDTO;
import com.sh.sh.pos.system.repository.BranchRepository;
import com.sh.sh.pos.system.repository.warehousesRepository.WarehouseRepository;
import com.sh.sh.pos.system.service.warehousesService.WarehouseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;

    @Override
    public WarehouseDTO create(WarehouseDTO dto) {

        if (warehouseRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Warehouse code already exists");
        }

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            warehouseRepository.findByBranchIdAndIsDefaultTrue(branch.getId())
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        warehouseRepository.save(existing);
                    });
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

        return WarehousesMapper.toDTO(
                warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseDTO update(Long id, WarehouseDTO dto) {

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setDescription(dto.getDescription());
        warehouse.setIsDefault(dto.getIsDefault());

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            warehouseRepository.findByBranchIdAndIsDefaultTrue(warehouse.getBranch().getId())
                    .ifPresent(existing -> {
                        // Avoid unsetting itself
                        if (!existing.getId().equals(warehouse.getId())) {
                            existing.setIsDefault(false);
                            warehouseRepository.save(warehouse);
                        }
                    });
        }
        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        warehouse.setDescription(dto.getDescription());
        warehouse.setIsDefault(dto.getIsDefault());

        return WarehousesMapper.toDTO(
                warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseDTO getById(Long id) {
        return WarehousesMapper.toDTO(
                warehouseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Warehouse not found")));
    }

    @Override
    public List<WarehouseDTO> getAll() {
        return WarehousesMapper.toDTOList(
                warehouseRepository.findByActiveTrue());
    }

    @Override
    public List<WarehouseDTO> getByBranch(Long branchId) {
        return WarehousesMapper.toDTOList(
                warehouseRepository.findByBranchIdAndActiveTrue(branchId));
    }

    @Override
    public WarehouseDTO getDefaultWarehouse(Long branchId) {

        return WarehousesMapper.toDTO(
                warehouseRepository.findByBranchIdAndIsDefaultTrue(branchId)
                        .orElseThrow(() -> new RuntimeException("Default warehouse not found")));
    }

    @Override
    public void deactivate(Long id) {

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        warehouse.setActive(false);

        warehouseRepository.save(warehouse);
    }
}