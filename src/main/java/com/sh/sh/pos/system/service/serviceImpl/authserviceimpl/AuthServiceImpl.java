package com.sh.sh.pos.system.service.serviceimpl.authserviceimpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sh.sh.pos.system.configuration.JwtProvider;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.authservice.AuthService;
import com.sh.sh.pos.system.service.authservice.SmsService;
import com.sh.sh.pos.system.service.emailservice.EmailService;
import com.sh.sh.pos.system.service.redisservice.AccountVerificationRedisService;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;
import com.sh.sh.pos.system.service.redisservice.PasswordResetRedisService;
import com.sh.sh.pos.system.service.redisservice.RefreshTokenRedisService;
import com.sh.sh.pos.system.service.serviceimpl.customeruserimpl.CustomUserImpl;
import com.sh.sh.pos.system.validator.PasswordValidator;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final String USER_NOT_FOUND = "User not found";
	private static final String DEFAULT_DEVICE_ID = "WEB";
	private static final long REFRESH_TOKEN_TTL_MS = 7L * 24 * 60 * 60 * 1000;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final CustomUserImpl customUserImpl;
	private final PasswordResetRedisService passwordResetRedisService;
	private final EmailService emailService;
	
	private final PasswordValidator passwordValidator;

	private final SmsService smsService;
	private final RefreshTokenRedisService refreshTokenRedisService;
	private final JwtBlackListService jwtBlackListService;

		private final AccountVerificationRedisService accountVerificationRedisService;

@Transactional
	@Override
	public AuthResponse register(UserDTO userDTO) {
		
		if (userDTO == null) {
			throw new UserException(
					"User data is required");
		}

		if (userDTO.getFullName() == null || userDTO.getFullName().isBlank()) {
			throw new UserException("FullName is required");
		}

		// normalize email
		String email = normalizeEmail(userDTO.getEmail());

		String fullName = userDTO.getFullName().trim();

		if (email == null || email.isBlank()) {
			throw new UserException(
"Email is required");
		}

		if (userDTO.getPassword() == null
				|| userDTO.getPassword().isBlank()) {
			throw new UserException("Password is required");
		}

		passwordValidator.validate(userDTO.getPassword());

		if (userDTO.getRole() == null) {
			throw new UserException("Role is required");
		}
// pubic registration must not create admin
		if (userDTO.getRole() == UserRole.ROLE_ADMIN) {
			throw new UserException(
					"Admin role is not allowed during public registration");
		}

		// normalize phone
		String phone = normalizePhone(userDTO.getPhone());

		if (phone != null && !phone.isBlank()) {
			if (userRepository.existsByPhone(phone)) {
				throw new UserException("Phone number already registered");
			}
		}

		if (userRepository.existsByEmail(email)) {
			throw new UserException(
					"Email already registered!");
		}

// Create user
		User newUser = new User();
		newUser.setEmail(email);
		newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		newUser.setRole(userDTO.getRole());
		newUser.setFullName(userDTO.getFullName().trim());
		newUser.setPhone(phone);
		newUser.setVerified(false);

		User savedUser = userRepository.save(newUser);

		String otp = generateOtp();
		accountVerificationRedisService.saveOtp(email, otp);

		try {
			emailService.sendVerificationOtpEmail(
				savedUser.getEmail(),
				savedUser.getFullName(),
					otp);

		} catch (RuntimeException e) {
			accountVerificationRedisService.deleteOtp(email);
			log.error("Failed to send verification email for user={}", savedUser.getId());
			throw new UserException(
					"Unable to send verification email. Please try again.");
		}

		AuthResponse authResponse = new AuthResponse();
				authResponse.setUser(UserMapper.toDTO(savedUser));
authResponse.setMessage("User registered successfully. Please check your email for the verification code.");
		authResponse.setTitle("Account verification required");
		return authResponse;

	}

	@Override
	public void verifyAccount(String identifier, String otp) {
		String normalizedIdentifier = normalizeIdentifier(identifier);
		if (normalizedIdentifier == null || normalizedIdentifier.isBlank()) {
			throw new UserException("Email or phone number is required");
		}

		if (otp == null || !otp.trim().matches("^\\d{6}$")) {
			throw new UserException("OTP must contain exactly 6 digits");
		}

		// This registration flow sends verification OTPs to email.
		if (!normalizedIdentifier.contains("@")) {
			throw new UserException(
					"Please use the email address associated with your account");
		}

		if (accountVerificationRedisService.hasExceededOtpAttempts(
				normalizedIdentifier)) {
			throw new UserException(
					"Too many invalid OTP attempts. Please request a new OTP.");
		}

		String storedOtp = accountVerificationRedisService.getOtp(normalizedIdentifier);

		if (storedOtp == null) {
			throw new UserException("OTP is invalid or expired");
		}

		if (!storedOtp.equals(otp.trim())) {
			accountVerificationRedisService.incrementOtpAttempts(
					normalizedIdentifier);
			throw new UserException("Invalid OTP");
		}

		User user = userRepository.findByEmail(normalizedIdentifier)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));

		if (Boolean.TRUE.equals(user.getVerified())) {
			accountVerificationRedisService.deleteOtp(normalizedIdentifier);
			return;
		}

		user.setVerified(true);
		userRepository.save(user);

		// OTP is single-use.
		accountVerificationRedisService.deleteOtp(normalizedIdentifier);

		log.info(
				"Account verified successfully for userId={}",
				user.getId());
	}

	// Resend verification OTP

	@Override
	public void resendVerificationOtp(String identifier) {
		String email = normalizeEmail(identifier);
		if (email == null || email.isBlank()) {
			throw new UserException("Email is required");
		}
		User user = userRepository.findByEmail(email).orElse(null);
		if (user == null) {
			return;
		}

		if (Boolean.TRUE.equals(user.getVerified())) {
			throw new UserException("Account is already verified");
		}

		String otp = generateOtp();

		accountVerificationRedisService.saveOtp(email, otp);

		try {
			emailService.sendVerificationOtpEmail(
					user.getEmail(),
					user.getFullName(),
					otp);
			log.info("Verification OTP resent successfully → email={}", user.getEmail());
		} catch (RuntimeException e) {
			log.error(
					"Failed to queue verification email for userId={}",
					user.getId(),
					e);

			throw new UserException(
					"Unable to send verification email. Please try again.");
		}
		log.info(
				"Verification OTP queued for userId={}",
				user.getId());
	}

	@Override
	public AuthResponse login(LoginDto dto) {
		
		if (dto == null) {
			throw new UserException("Login data is required");
		}

		String identifier = normalizeIdentifier(dto.getIdentifier());
		if (identifier == null || identifier.isBlank()) {
			throw new UserException("Email or phone number is required");
		}

		if (dto.getPassword() == null || dto.getPassword().isBlank()) {
			throw new UserException("Password is required");
		}

		String deviceId = normalizeDeviceId(dto.getDeviceId());

		Authentication authentication = authenticate(identifier, dto.getPassword());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		String accessToken = jwtProvider.generateAccessToken(authentication);
		String refreshToken = jwtProvider.generateRefreshToken(authentication);

		User user = findUserByIdentifier(identifier);

		if (!Boolean.TRUE.equals(user.getVerified())) {
			throw new UserException("Your account is not verified. Please verify your account first.");
		}

		// Save session in Redis
		refreshTokenRedisService.save(user.getEmail(), deviceId, refreshToken, REFRESH_TOKEN_TTL_MS);

		user.setLastLogin(LocalDateTime.now());
		userRepository.save(user);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setMessage("Welcome: " + user.getFullName());
		authResponse.setUser(UserMapper.toDTO(user));
		authResponse.setTitle("Login success");
log.info("Login successful → identifier={} email={} device={}", identifier, user.getEmail(), deviceId);
		return authResponse;
	}

// Authentication user
	private Authentication authenticate(String identifier, String password) {
		
		if (identifier == null || identifier.isBlank()) {
			throw new UserException("Email or phone number is required");
		}

		UserDetails userDetails;

		try {
userDetails = customUserImpl.loadUserByUsername(identifier);

		} catch (Exception e) {
			log.warn("Authentication failed - identifier={} - reason={}", identifier, e.getMessage());
			log.debug("Authentication exception for identifier={}", identifier, e);
			throw new UserException("Invalid credentials");
		}

		if (userDetails == null) {
			log.warn("Authentication failed - no user found for identifier={}", identifier);
			throw new UserException("Invalid email/phone number or password");
		}

		boolean passwordMatches = passwordEncoder.matches(password, userDetails.getPassword());
		if (!passwordMatches) {
			log.warn("Authentication failed - invalid password for identifier={}", identifier);
			throw new UserException("Invalid email/phone number or password");
		}
		log.info("Authentication successful - identifier={}", identifier);
		return createAuthentication(userDetails);
	}

	// Create Authentication
	private Authentication createAuthentication(
			UserDetails userDetails) {

		if (userDetails == null) {
			throw new UserException("User details are required");
		}

		return new UsernamePasswordAuthenticationToken(
				userDetails,
null,
userDetails.getAuthorities());
	}

	// Find User By email or phone

	private User findUserByIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			throw new UserException("Email or phone number is required");
		}

		if (identifier.contains("@")) {
			String email = normalizeEmail(identifier);

			return userRepository.findByEmail(email).orElseThrow(() -> new UserException(USER_NOT_FOUND));
		}

		String phone = normalizePhone(identifier);
		return userRepository.findByPhone(phone).orElseThrow(() -> new UserException(USER_NOT_FOUND));
	}

	// Password Reset-Create otp

	@Override
	public void createPasswordResetOtp(String identifier) throws MessagingException {
		String normalizedIdentifier = normalizeIdentifier(identifier);

		if (normalizedIdentifier == null || normalizedIdentifier.isBlank()) {
			throw new UserException("Email or Phone number is required");
		}
		User user = findUserByIdentifier(normalizedIdentifier);
		String otp = generateOtp();

		passwordResetRedisService.saveOtp(normalizedIdentifier, otp);

		if (normalizedIdentifier.contains("@")) {
			sendOtpByEmail(user, otp);
			log.info("Password reset OTP sent by email → email={}", user.getEmail());
		} else {
			sendOtpBySms(user, otp);
			log.info("Password reset OTP sent by SMS → phone={}", user.getPhone());
		}
	}

	private void sendOtpByEmail(
			User user,
			String otp)
			throws MessagingException {

		emailService.sendPasswordResetOtpEmail(
				user.getEmail(),
				user.getFullName(),
				otp);
	}

	private String generateOtp() {
		int otp = 100000 + new java.security.SecureRandom().nextInt(900000);
		return String.valueOf(otp);
	}

	@Override
	public void resetPassword(String resetToken, String newPassword) {
		
		if (resetToken == null || resetToken.isBlank()) {
			throw new UserException("Reset token is required");
		}

		passwordValidator.validate(newPassword);

		String cleanResetToken = resetToken.trim();

		/* * Get email from Redis */
		String email = passwordResetRedisService.getEmailFromResetToken(cleanResetToken);
		if (email == null || email.isBlank()) {
			throw new UserException("Invalid or expired reset token");
		}

email = normalizeEmail(email);

		/* * Find user */
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserException(USER_NOT_FOUND));

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

				passwordResetRedisService.deleteResetToken(cleanResetToken);
		log.info("Password reset successful → userId={}", user.getId());
	}

	// Refresh Token

	@Override
@Transactional
	public AuthResponse refreshToken(
			String refreshToken,
			String deviceId) {

		// 1. Validate refresh token input

		if (refreshToken == null || refreshToken.isBlank()) {
			throw new UserException("Refresh token required");
		}

		// Normalize Bearer token
		String normalizedToken = refreshToken.trim();

		if (normalizedToken.startsWith("Bearer ")) {
			normalizedToken = normalizedToken
					.substring(7)
					.trim();
		}

		if (normalizedToken.isBlank()) {
			throw new UserException("Refresh token required");
		}

		// 2. Normalize device ID

		String normalizedDeviceId = normalizeDeviceId(deviceId);

		// 3. Validate JWT refresh token

		if (!jwtProvider.validateRefreshToken(normalizedToken)) {
			throw new UserException(
					"Invalid or expired refresh token");
		}

		// 4. Get userId from JWT

		Long userId;

		try {
			userId = jwtProvider.getUserIdFromToken(normalizedToken);
		} catch (Exception e) {
			log.warn("Refresh token contains invalid user ID");
			throw new UserException("Invalid refresh token");

		}

		// 5. Find user

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));

		String email = normalizeEmail(user.getEmail());
		// 6. Validate Redis refresh session

		boolean valid = refreshTokenRedisService.isValid(
email,
				normalizedDeviceId,
				normalizedToken);

		if (!valid) {

			log.warn(
					"Refresh token rejected → userId={} email={} device={}",
					userId,
					email,
					normalizedDeviceId);

			throw new UserException(
"Session expired or logged out");
		}

		// 7. Load UserDetails

		UserDetails userDetails = customUserImpl.loadUserByUsername(email);

// 8. Create Authentication

		Authentication authentication = createAuthentication(userDetails);

		// 9. Generate new access + refresh tokens

		String newAccessToken = jwtProvider.generateAccessToken(
				authentication);

		String newRefreshToken = jwtProvider.generateRefreshToken(
				authentication);

		// =========================================================
		// 10. Rotate refresh token
		// =========================================================

		refreshTokenRedisService.delete(
				email,
				normalizedDeviceId);

		refreshTokenRedisService.save(
email,
				normalizedDeviceId,
newRefreshToken,
REFRESH_TOKEN_TTL_MS);

		// =========================================================
		// 11. Build response
		// =========================================================

		AuthResponse response = new AuthResponse();

		response.setAccessToken(newAccessToken);
		response.setRefreshToken(newRefreshToken);
		response.setMessage("Token refreshed successfully");
		response.setUser(UserMapper.toDTO(user));

log.info("Refresh successful → email={} device={}", email, normalizedDeviceId);
		return response;
	}

	@Override
	public void logout(String token, String email, String deviceId) {

		if (token == null || token.isBlank()) {

			throw new UserException(
					"Access token is required");
		}

		// remove Bearer prefix
		String rawToken = token.trim();
		if (rawToken.startsWith("Bearer ")) {
			rawToken = rawToken.substring(7).trim();
		}

		String actualDeviceId = normalizeDeviceId(deviceId);

		// validate token
		if (!jwtProvider.validateAccessToken(rawToken)) {
			throw new UserException("Invalid or expired access token");
		}

		// Get user ID from Jwt
		Long userId;

		try {
			userId = jwtProvider.getUserIdFromToken(rawToken);
		} catch (Exception e) {
			throw new UserException("Invalid access token");

		}

		String tokenEmail = jwtProvider.getEmailFromToken(
				rawToken);

		if (tokenEmail == null || tokenEmail.isBlank()) {
			throw new UserException("Invalid access token");
		}
		tokenEmail = normalizeEmail(tokenEmail);

		// passes email from request body and compare with the jwt
		if (email != null
				&& !email.isBlank()
				&& !tokenEmail.equalsIgnoreCase(
						email.trim())) {

			throw new UserException(
					"Token user does not match requested user");
		}

		// blacklist for the remaining expiration time
		long remainingTime = jwtProvider.getRemainingExpiration(rawToken);
if (remainingTime > 0) {
		jwtBlackListService.blacklistToken(rawToken, remainingTime);
}

		// remove refresh token session from redis
		refreshTokenRedisService.delete(tokenEmail, actualDeviceId);

		SecurityContextHolder.clearContext();

		log.info("User logged out — token blacklisted,userId={} email={} device={}", userId, tokenEmail,
				actualDeviceId);
	}

	@Override
	public UserDTO getCurrentUser(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UserException("Authentication Required");
		}

		String principalName = authentication.getName();
		if (principalName == null || principalName.isBlank()) {
			throw new UserException("Authenticated user ID is missing");
		}

		Long userId;

		try {
			userId = Long.valueOf(principalName);
		} catch (NumberFormatException e) {
			throw new UserException("Authenticated user ID is invalid");
	}

		User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
		return UserMapper.toDTO(user);
	}

	private void sendOtpBySms(User user, String otp) {
		smsService.sendOtp(user.getPhone(), otp);
	}

	private String normalizeIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			return null;
		}
		String value = identifier.trim();
		/* * Email */
		if (value.contains("@")) {
			return normalizeEmail(value);
		}
		/* * Phone */
		return normalizePhone(value);
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase();
	}

	private String normalizePhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return null;
		}
		String value = phone.trim();

		value = value.replaceAll("\\s+", "");
		value = value.replace("-", "");
		value = value.replace("(", "");
		value = value.replace(")", "");
		/* * Cambodia local format: * * 012345678 * * -> +85512345678 */
		if (value.startsWith("0")) {
			value = "+855" + value.substring(1);
		}
		/* * International format without + * * 85512345678 * * -> +85512345678 */
		else if (value.startsWith("855")) {
			value = "+" + value;
		}
		return value;
	} // ========================================================= // NORMALIZE DEVICE
		// ID // ========================================================= private
		//

	String normalizeDeviceId(String deviceId) {
		if (deviceId == null ||
				deviceId.isBlank()) {
			return DEFAULT_DEVICE_ID;
		}
		return deviceId.trim()
				.replaceAll("\\s+", "_").toUpperCase();
	}

	@Override
	public String verifyPasswordResetOtp(String identifier, String otp) {

		String normalizedIdentifier = normalizeIdentifier(identifier);
		if (normalizedIdentifier == null || normalizedIdentifier.isBlank()) {
			throw new UserException("Email or phone number is required");
		}
		if (otp == null || otp.isBlank()) {
			throw new UserException("OTP is required");
		}

		String cleanOtp = otp.trim();

		/* * Check maximum attempts */

		if (passwordResetRedisService.hasExceededOtpAttempts(normalizedIdentifier)) {
			throw new UserException("Too many invalid OTP attempts. Please request a new OTP.");
		}
		/* * Get OTP from Redis */
		String storedOtp = passwordResetRedisService.getOtp(normalizedIdentifier);
		if (storedOtp == null) {
			throw new UserException("OTP is invalid or expired");
		}
		/* * Check OTP */
		if (!storedOtp.equals(cleanOtp)) {
			long attempts = passwordResetRedisService.incrementOtpAttempts(normalizedIdentifier);
			log.warn("Invalid password reset OTP → identifier={} attempts={}", normalizedIdentifier, attempts);
			throw new UserException("Invalid OTP");
		}

		/* * OTP is correct. * * Find user again. */
		User user = findUserByIdentifier(normalizedIdentifier);

		/* * Generate temporary reset token */
		String resetToken = UUID.randomUUID().toString();

		passwordResetRedisService.saveResetToken(resetToken, user.getEmail());

		passwordResetRedisService.deleteOtp(normalizedIdentifier);
		log.info("Password reset OTP verified successfully → userId={}", user.getId());
		return resetToken;
	}

}