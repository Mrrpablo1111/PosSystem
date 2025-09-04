package com.sh.sh.pos.system.mapper;

import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;

public class UserMapper {
	
	public static UserDTO toDTO(User savedUser) {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(savedUser.getId());
		userDTO.setFullName(savedUser.getFullName());
		userDTO.setEmail(savedUser.getEmail());
		userDTO.setRole(savedUser.getRole());
		userDTO.setPhone(savedUser.getPhone());
		userDTO.setLastLogin(savedUser.getLastLogin());
		userDTO.setCreatedAt(savedUser.getCreatedAt());
		userDTO.setUpdatedAt(savedUser.getUpdatedAt());
		
		
		return userDTO;
	}
}
