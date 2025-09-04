package com.sh.sh.pos.system.payload.response;

import com.sh.sh.pos.system.payload.dto.UserDTO;

import lombok.Data;
 

@Data
public class AuthResponse {
	private String jwt;
	private String message;
	private UserDTO user;

}
