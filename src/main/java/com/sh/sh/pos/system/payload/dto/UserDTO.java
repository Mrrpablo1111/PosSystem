package com.sh.sh.pos.system.payload.dto;

import java.time.LocalDateTime;

import com.sh.sh.pos.system.domain.UserRole;


import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserDTO {
	
	    private Long id;
	    private String fullName;
	    private String email;
	    private String phone;
	    private UserRole role;
		private String username;
	    private String password;
	    
	    private Long branchId;
	    private Long storeId;

		private BranchDTO branch;
		private String branchName;
	    
	    private LocalDateTime lastLogin;


		public UserDTO(Long id, String fullName, String email, UserRole role, String branchName, LocalDateTime lastLogin){
			this.id = id;
			this.fullName = fullName;
			this.email = email;
			this.role = role;
			this.password = null;
			this.phone = null;
			this.username  = null;
			this.storeId = null;
			this.branchId = null;
			this.branch = null;
			this.branchName = branchName;
			this.lastLogin = lastLogin;
			
		}
}
