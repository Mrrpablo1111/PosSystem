package com.sh.sh.pos.system.controller.authcontroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sh.sh.pos.system.configuration.JwtProvider;

import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.ForgotPasswordDto;

import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.request.RefreshRequestDto;
import com.sh.sh.pos.system.payload.request.ResetPasswordDto;
import com.sh.sh.pos.system.payload.response.ApiResponseBody;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.payload.response.MessageResponse;
import com.sh.sh.pos.system.service.authservice.AuthService;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final JwtBlackListService jwtBlackListService;
	private final JwtProvider jwtProvider;

	@PostMapping("/register")
	public ResponseEntity<ApiResponseBody<AuthResponse>> registerHandler(@RequestBody @Valid UserDTO userDTO)
			 {

		AuthResponse res = authService.register(userDTO);

		return ResponseEntity.ok(new ApiResponseBody<>(true, "User created successfully", res));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponseBody<AuthResponse>> loginHandler(@RequestBody LoginDto userDTO)
			{

		AuthResponse res = authService.login(userDTO);
		return ResponseEntity.ok(new ApiResponseBody<>(true, "User logged in successfully", res));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(
			@Valid @RequestBody ForgotPasswordDto req) throws MessagingException {

		authService.createPasswordResetToken(req.getEmail());

		return ResponseEntity.ok(
				new MessageResponse("If an account exists with that email, a reset link has been sent"));
	}
	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(
			@RequestBody ResetPasswordDto req)  {

		authService.resetPassword(req.getToken(), req.getNewPassword());

		return ResponseEntity.ok(
				new MessageResponse("Password reset successfully"));
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(
			HttpServletRequest request) {

		String bearerToken = request.getHeader("Authorization");

		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {

			String token = bearerToken.substring(7);

			long expiration = jwtProvider.getRemainingExpiration(token);

			jwtBlackListService.blacklistToken(token, expiration);
		}

		return ResponseEntity.ok(
				new MessageResponse("Logout successful"));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<ApiResponseBody<AuthResponse>> refreshToken(
			@RequestBody RefreshRequestDto dto) {

		AuthResponse res = authService.refreshToken(
				dto.getRefreshToken(),
				dto.getDeviceId());

		return ResponseEntity.ok(
				new ApiResponseBody<>(
						true,
						"Token refreshed successfully",
						res));
	}
}
