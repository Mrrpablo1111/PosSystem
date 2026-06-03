package com.sh.sh.pos.system.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		userDTO.setPhone(savedUser.getPhone());
		userDTO.setBranchId(savedUser.getBranch()!=null? savedUser.getBranch().getId(): null);
		userDTO.setBranch(savedUser.getBranch()==null?null:BranchMapper.toDTO(savedUser.getBranch()));
		userDTO.setStoreId(savedUser.getStore()!=null? savedUser.getStore().getId():  null);
		
		
		
		return userDTO;
	}
	
	public static List<UserDTO> toDTOList(List<User> users){
		return users.stream().map(UserMapper::toDTO).collect(Collectors.toList());
	}


	public static Set<UserDTO> toDTOSet(Set<User> users){
		return users.stream().map(UserMapper::toDTO).collect(Collectors.toSet());
	}
	public static User toEntity(UserDTO userDTO) {
		
		User createdUser = new User();
		
		
		createdUser.setEmail(userDTO.getEmail());
		createdUser.setFullName(userDTO.getFullName());
		createdUser.setRole(userDTO.getRole());
		createdUser.setLastLogin(userDTO.getLastLogin());
		createdUser.setPhone(userDTO.getPhone());
		createdUser.setPassword(userDTO.getPassword());
		
		return createdUser ;
	}
}
