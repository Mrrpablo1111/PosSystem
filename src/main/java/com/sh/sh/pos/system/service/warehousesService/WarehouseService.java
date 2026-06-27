package com.sh.sh.pos.system.service.warehousesService;

import java.util.List;

import com.sh.sh.pos.system.payload.dto.warehousesDTO.WarehouseDTO;

public interface WarehouseService {

    WarehouseDTO create(WarehouseDTO dto);

    WarehouseDTO update(Long id, WarehouseDTO dto);

    WarehouseDTO getById(Long id);

    List<WarehouseDTO> getAll();

    List<WarehouseDTO> getByBranch(Long branchId);

    WarehouseDTO getDefaultWarehouse(Long branchId);

    void deactivate(Long id);
}