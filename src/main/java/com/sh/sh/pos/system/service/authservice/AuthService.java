 package com.sh.sh.pos.system.service.authservice;


import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;

import jakarta.mail.MessagingException;


public interface AuthService {
	AuthResponse register(UserDTO userDTO);

	AuthResponse login(LoginDto dto);

	void createPasswordResetToken(String email) throws MessagingException;

	void resetPassword(String token, String newPassword);

	AuthResponse refreshToken(String refreshToken, String deviceId);

	void logout(String token, String email, String deviceId);


}
