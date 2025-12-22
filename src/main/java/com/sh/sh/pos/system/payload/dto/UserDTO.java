package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.UserRole;

import lombok.Data;


@Data
public class UserDTO {
	
	    private Long id;
	    private String fullName;
	    private String email;
	    private String phone;
	    private UserRole role;

	    private String password;
	    
	    private Long branchId;
	    private Long storeId;
	    
	    private LocalDateTime createdAt;
	    private LocalDateTime updatedAt;
	    private LocalDateTime lastLogin;
}
