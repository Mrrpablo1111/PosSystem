package com.sh.sh.pos.system.service.serviceimpl.authserviceimpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.configuration.emailtemplate.EmailTemplateBuilder;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.mapper.usermapper.UserMapper;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.dto.userdto.UserDTO;
import com.sh.sh.pos.system.payload.request.LoginDto;
import com.sh.sh.pos.system.payload.response.AuthResponse;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.authservice.AuthService;
import com.sh.sh.pos.system.service.emailservice.EmailService;
import com.sh.sh.pos.system.service.redisservice.JwtBlackListService;
import com.sh.sh.pos.system.service.redisservice.PasswordResetRedisService;
import com.sh.sh.pos.system.service.redisservice.RefreshTokenRedisService;
import com.sh.sh.pos.system.service.serviceimpl.customeruserimpl.CustomUserImpl;

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
	private final EmailTemplateBuilder templateBuilder;
	private final RefreshTokenRedisService refreshTokenRedisService;
	private final JwtBlackListService jwtBlackListService;

	@Value("${app.frontend.reset-password-url:http://localhost:5173/reset-password}")
	private String resetPasswordUrl;

	@Override
	public AuthResponse register(UserDTO userDTO) {
		User existing = userRepository.findByEmail(userDTO.getEmail());

		if (existing != null) {
			throw new UserException("Email already registered!");
		}

		if (userDTO.getRole() == UserRole.ROLE_ADMIN) {
			throw new UserException("Role Admin not allowed!");
		}

		User newUser = new User();
		newUser.setEmail(userDTO.getEmail());
		newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		newUser.setRole(userDTO.getRole());
		newUser.setFullName(userDTO.getFullName());
		newUser.setPhone(userDTO.getPhone());
		newUser.setLastLogin(LocalDateTime.now());

		User savedUser = userRepository.save(newUser);

		// load real authorities
		UserDetails userDetails = customUserImpl.loadUserByUsername(savedUser.getEmail());

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				savedUser.getEmail(),
				null,
				userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String accessToken = jwtProvider.generateAccessToken(authentication);
		String refreshToken = jwtProvider.generateRefreshToken(authentication);

		refreshTokenRedisService.save(
				savedUser.getEmail(),
				DEFAULT_DEVICE_ID,
				refreshToken,
				REFRESH_TOKEN_TTL_MS);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setTitle("Welcome: " + newUser.getEmail());
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setMessage("Registered Successfully");
		authResponse.setUser(UserMapper.toDTO(savedUser));
		return authResponse;
	}

	@Override
	public AuthResponse login(LoginDto dto) {
		String email = dto.getEmail();
		String password = dto.getPassword();
		String deviceId = dto.getDeviceId() != null ? dto.getDeviceId() : DEFAULT_DEVICE_ID;

		Authentication authentication = authenticate(email, password);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		String accessToken = jwtProvider.generateAccessToken(authentication);
		String refreshToken = jwtProvider.generateRefreshToken(authentication);

		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserException(USER_NOT_FOUND);
		}

		// Save session in Redis
		refreshTokenRedisService.save(email, deviceId, refreshToken, REFRESH_TOKEN_TTL_MS);

		user.setLastLogin(LocalDateTime.now());
		userRepository.save(user);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setAccessToken(accessToken);
		authResponse.setRefreshToken(refreshToken);
		authResponse.setMessage("Welcome: " + email);
		authResponse.setUser(UserMapper.toDTO(user));
		authResponse.setTitle("Login success");

		return authResponse;
	}

	private Authentication authenticate(String email, String password) {
		UserDetails userDetails = customUserImpl.loadUserByUsername(email);

		if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
			throw new UserException("Invalid email or password");
		}
		return new UsernamePasswordAuthenticationToken(email, null, userDetails.getAuthorities());
	}

	@Override
	public void createPasswordResetToken(String email) throws MessagingException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
        throw new UserException("No account found with email: " + email);
    }
		String token = UUID.randomUUID().toString();

		passwordResetRedisService.saveToken(token, user.getEmail());

		String link = resetPasswordUrl + "?token=" + token;
		String html = templateBuilder.buildResetPasswordEmail(link);

		emailService.sendEmail(user.getEmail(), "Reset Password - SH POS", html);

		log.info("Password reset token created for email={}", user.getEmail());
	}

	@Override
	public void resetPassword(String token, String newPassword) {
		String email = passwordResetRedisService.getEmail(token);

		if (email == null) {
			throw new UserException("Invalid or expired token");
		}

		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException(USER_NOT_FOUND);
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		// delete token after use (VERY IMPORTANT)
		passwordResetRedisService.deleteToken(token);

		log.info("Password reset success for email={} — token removed from Redis", email);
	}

	@Override
	public AuthResponse refreshToken(String refreshToken, String deviceId) {

		String type = jwtProvider.getTokenType(refreshToken);

		if (!"REFRESH".equals(type)) {
			throw new UserException("Invalid refresh token");
		}

		String email = jwtProvider.getEmailFromToken(refreshToken);

		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException(USER_NOT_FOUND);
		}

		// check Redis session
		boolean valid = refreshTokenRedisService.isValid(email, deviceId, refreshToken);

		if (!valid) {
			throw new UserException("Session expired or logged out");
		}

		UserDetails userDetails = customUserImpl.loadUserByUsername(email);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				email,
				null,
				userDetails.getAuthorities());

		// ROTATE refresh token
		String newAccessToken = jwtProvider.generateAccessToken(authentication);
		String newRefreshToken = jwtProvider.generateRefreshToken(authentication);

		refreshTokenRedisService.save(email, deviceId, newRefreshToken, REFRESH_TOKEN_TTL_MS);

		AuthResponse response = new AuthResponse();
		response.setAccessToken(newAccessToken);
		response.setRefreshToken(newRefreshToken);
		response.setMessage("Token refreshed successfully");
		response.setUser(UserMapper.toDTO(user));

		return response;
	}

	@Override
	public void logout(String token, String email, String deviceId) {

		// remove Bearer prefix
		String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;

		// validate token
		if (!jwtProvider.validateToken(rawToken)) {
			throw new UserException("Invalid token");
		}

		String type = jwtProvider.getTokenType(rawToken);

		if (!"ACCESS".equals(type)) {
			throw new UserException("Logout requires access token");
		}

		// blacklist for the remaining expiration time
		long remainingTime = jwtProvider.getRemainingExpiration(rawToken);
		jwtBlackListService.blacklistToken(rawToken, remainingTime);

		// remove refresh token session from redis
		refreshTokenRedisService.delete(email, deviceId);

		log.info("User logged out — token blacklisted, email={} device={}", email, deviceId);
	}
}