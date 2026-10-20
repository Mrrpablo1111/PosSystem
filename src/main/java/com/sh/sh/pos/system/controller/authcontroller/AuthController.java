package com.sh.sh.pos.system.controller.authcontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.ForgotPasswordRequest;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.request.LogoutRequest;
import com.sh.sh.pos.system.payload.request.RefreshRequestDto;
import com.sh.sh.pos.system.payload.request.ResetPasswordDto;
import com.sh.sh.pos.system.payload.request.VerifyAccountRequest;
import com.sh.sh.pos.system.payload.request.VerifyOtpRequest;
import com.sh.sh.pos.system.payload.response.ApiResponseBody;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.payload.response.MessageResponse;
import com.sh.sh.pos.system.service.authservice.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;
	

	@PostMapping("/register")
	public ResponseEntity<ApiResponseBody<AuthResponse>> registerHandler(@RequestBody @Valid UserDTO userDTO)
			 {

		AuthResponse res = authService.register(userDTO);

		return ResponseEntity.ok(new ApiResponseBody<>(true, "User created successfully", res));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponseBody<AuthResponse>> loginHandler(@RequestBody LoginDto userDTO) {

		AuthResponse res = authService.login(userDTO);
		return ResponseEntity.ok(new ApiResponseBody<>(true, "User logged in successfully", res));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		try {
			authService.createPasswordResetOtp(request.getIdentifier());
			/* * Don't reveal whether the account exists. */
			return ResponseEntity
					.ok(Map.of("message", "If the account exists, an OTP has been sent."));
		} catch (Exception e) {
			/* * Don't expose account existence. */
			log.warn("Password reset request failed");
			return ResponseEntity.ok(Map.of("message", "If the account exists, an OTP has been sent."));
		}
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
		String resetToken = authService.verifyPasswordResetOtp(request.getIdentifier(), request.getOtp());
		return ResponseEntity.ok(Map.of("message", "OTP verified successfully", "resetToken", resetToken));
	}

	@PostMapping("/verify-account")
	public ResponseEntity<ApiResponseBody<MessageResponse>> verifyAccount(
			@Valid @RequestBody VerifyAccountRequest request) {

		authService.verifyAccount(
				request.getEmail(),
				request.getOtp());

		MessageResponse message = new MessageResponse(
				"Account verified successfully. You can now log in.");

		return ResponseEntity.ok(
				new ApiResponseBody<>(
						true,
						"Account verified successfully",
						message));
	}

	@PostMapping("/resend-verification-otp")
	public ResponseEntity<MessageResponse> resendVerificationOtp(
			@Valid @RequestBody ForgotPasswordRequest request) {

		authService.resendVerificationOtp(
				request.getIdentifier());

		return ResponseEntity.ok(
				new MessageResponse(
						"If the account exists and still needs verification, "
								+ "a new verification code has been sent."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto request) {
		authService.resetPassword(request.getResetToken(), request.getNewPassword());
		return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
	}

	@PostMapping("/logout")
	public ResponseEntity<MessageResponse> logout(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestBody(required = false) LogoutRequest request) {

		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Authorization token is required"));
		}

		String deviceId = request == null ? "WEB" : request.getDeviceId();
		String email = request == null ? null : request.getEmail();
		authService.logout(authorizationHeader, email, deviceId);

		return ResponseEntity.ok(new MessageResponse("Logout successful"));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<ApiResponseBody<AuthResponse>> refreshToken(@RequestBody RefreshRequestDto dto) {
		AuthResponse res = authService.refreshToken(dto.getRefreshToken(), dto.getDeviceId());
		return ResponseEntity.ok(new ApiResponseBody<>(true, "Token refreshed successfully", res));
	}

	@GetMapping("/users")
	public ResponseEntity<ApiResponseBody<UserDTO>> getCurrentUser(Authentication authentication) {

		UserDTO user = authService.getCurrentUser(authentication);

		return ResponseEntity.ok(new ApiResponseBody<>(true, "Current user retrieved succesfully", user));

	}
}
