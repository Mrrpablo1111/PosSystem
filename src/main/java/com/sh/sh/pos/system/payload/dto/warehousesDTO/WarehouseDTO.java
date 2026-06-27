package com.sh.sh.pos.system.payload.dto.warehousesDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO{
    private Long id;
    private String code;
    private String name;
    private String address;
    private String description;
    private Boolean isDefault;
    private Boolean active;
    private Long branchId;
    private String branchName;

}
