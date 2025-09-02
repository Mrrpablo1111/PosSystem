package com.sh.sh.pos.system.service;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.response.AuthResponse;

public interface AuthService {
	AuthResponse register(UserDTO userDTO) throws UserException;
	AuthResponse login(UserDTO userDTO) throws UserException; 

}
