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
		userDTO.setPhone(savedUser.getPhone());
		userDTO.setBranchId(savedUser.getBranch()!=null? savedUser.getBranch().getId(): null);
		userDTO.setStoreId(savedUser.getStore()!=null? savedUser.getStore().getId():  null);
		
		
		
		return userDTO;
	}
	
	
	public static User toEntity(UserDTO userDTO) {
		
		User createdUser = new User();
		
		
		createdUser.setEmail(userDTO.getEmail());
		createdUser.setFullName(userDTO.getFullName());
		createdUser.setRole(userDTO.getRole());
		createdUser.setCreatedAt(userDTO.getCreatedAt());
		createdUser.setUpdatedAt(userDTO.getUpdatedAt());
		createdUser.setLastLogin(userDTO.getLastLogin());
		createdUser.setPhone(userDTO.getPhone());
		createdUser.setPassword(userDTO.getPassword());
		
		return createdUser ;
	}
}
