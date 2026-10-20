 package com.sh.sh.pos.system.service.authservice;


import org.springframework.security.core.Authentication;

import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;

import jakarta.mail.MessagingException;


public interface AuthService {
	AuthResponse register(UserDTO userDTO);

	AuthResponse login(LoginDto dto);

	void resetPassword(String token, String newPassword);

	void verifyAccount(String identifier, String otp);

	void resendVerificationOtp(String identifier);

	AuthResponse refreshToken(String refreshToken, String deviceId);

	void logout(String token, String email, String deviceId);

	void createPasswordResetOtp(String identifier) throws MessagingException;

	String verifyPasswordResetOtp(String identifier, String otp);

	UserDTO getCurrentUser(Authentication authentication);


}
