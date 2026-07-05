package com.sh.sh.pos.system.payload.dto.storedto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.model.storecontact.StoreContact;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDTO {

	private Long id;
	private String brand;
	private String description;
	private String storeType;
	private StoreStatus status;
	private StoreContact contact;

	private Long storeAdminId;
	private UserDTO storeAdmin;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
