package com.sh.sh.pos.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> registerHandler(@RequestBody UserDTO userDTO)throws UserException{
		return ResponseEntity.ok(authService.register(userDTO));
	}
	
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> loginHandler(@RequestBody UserDTO userDTO) throws UserException{
		return ResponseEntity.ok(authService.login(userDTO));
	}

}
