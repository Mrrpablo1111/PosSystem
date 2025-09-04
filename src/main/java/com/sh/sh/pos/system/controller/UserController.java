package com.sh.sh.pos.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.mapper.UserMapper;
import com.sh.sh.pos.system.model.User;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	
	
	private final UserService userService;
	
	@GetMapping("/profile")
	public ResponseEntity<UserDTO> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException{
		User user = userService.getUserFromJwtToken(jwt);
		return ResponseEntity.ok(UserMapper.toDTO(user));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@RequestHeader("Authorization") String jwt,@PathVariable Long id) throws UserException{
		User user = userService.getUserById(id);
		if(user == null) {
			throw new UserException("user not found");
		}
		return ResponseEntity.ok(UserMapper.toDTO(user));
	}
	
}
