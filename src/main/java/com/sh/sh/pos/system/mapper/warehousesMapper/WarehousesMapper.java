package com.sh.sh.pos.system.mapper.warehousesMapper;

import java.util.List;
import java.util.stream.Collectors;

import com.sh.sh.pos.system.model.warehouses.Warehouse;
import com.sh.sh.pos.system.payload.dto.warehousesDTO.WarehouseDTO;

public class WarehousesMapper {
    public static WarehouseDTO toDTO(Warehouse warehouse){
        if(warehouse == null) return null;
        return WarehouseDTO.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .description(warehouse.getDescription())
                .isDefault(warehouse.getIsDefault())
                .active(warehouse.getActive())
                .branchId(warehouse.getBranch().getId())
                .branchName(warehouse.getBranch().getName())
                .build();
    }
    public static List<WarehouseDTO> toDTOList(List<Warehouse> warehouses){
        return warehouses.stream()
                .map(WarehousesMapper::toDTO)
                .collect(Collectors.toList());
    }
}
