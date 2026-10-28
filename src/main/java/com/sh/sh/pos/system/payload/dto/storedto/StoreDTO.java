package com.sh.sh.pos.system.payload.dto.storedto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.model.store.StoreType;
import com.sh.sh.pos.system.model.storecontact.StoreContact;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

	@NotBlank(message = "Brand name is required")
	@Size(max = 100, message = "Brand name must not exceed 100 characters")
	private String brand;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	private String description;

	// dynamic storeType
	private Long storeTypeId;
	private String storeTypeName;

	// Server controlled fields: do not trust these from client requests
	private StoreStatus status;

	@Valid
	private StoreContact contact;

	private Long storeAdminId;
	private UserDTO storeAdmin;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
