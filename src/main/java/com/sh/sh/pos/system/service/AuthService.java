 package com.sh.sh.pos.system.service;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.payload.dto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;

import jakarta.mail.MessagingException;


public interface AuthService {
	AuthResponse register(UserDTO userDTO ) throws UserException;
    AuthResponse login(LoginDto dto) throws UserException; 
	AuthResponse refreshToken(String refreshToken, String deviceId) throws UserException;
	void createPasswordResetToken(String email) throws UserException, MessagingException;
	void resetPassword(String token, String newPassword) throws UserException;
	void logout(String token,String email, String deviceId) throws UserException;


}
