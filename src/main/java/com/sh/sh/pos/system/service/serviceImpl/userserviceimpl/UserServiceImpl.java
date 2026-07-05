package com.sh.sh.pos.system.service.serviceimpl.userserviceimpl;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sh.sh.pos.system.configuration.JwtProvider;
import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.exception.ResourceNotFoundException;
import com.sh.sh.pos.system.exception.UserException;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.request.UpdateUserDto;
import com.sh.sh.pos.system.repository.userrepository.UserRepository;
import com.sh.sh.pos.system.service.userservice.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final String USER_NOT_FOUND = "User not found";

	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;

	@Override
	public User getUserFromJwtToken(String jwt) {
		String email = jwtProvider.getEmailFromToken(jwt);
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UserException("Invalid token");
		}
		return user;
	}

	@Override
	public User getCurrentUser() {
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getName() == null) {
			throw new UserException("No authenticated user in security context");
		}

		log.debug("Current authentication: name={} authorities={}",
				authentication.getName(), authentication.getAuthorities());

		User user = userRepository.findByEmail(authentication.getName());
		if (user == null) {
			throw new UserException(USER_NOT_FOUND);
		}
		return user;
	}

	@Override
	public User getUserByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserException(USER_NOT_FOUND);
		}
		return user;
	}

	@Override
	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	@Override
	public List<User> getAllUser() {
		return userRepository.findAll();
	}

	@Override
	public Set<User> getUserByRole(UserRole role) {
		return userRepository.findByRole(role);
	}

	@Override
	public User updateUser(UpdateUserDto updateData, User user) {
		if (updateData.getFullName() != null) {
			user.setFullName(updateData.getFullName());
		}
		if (updateData.getPhoneNumber() != null) {
			user.setPhone(updateData.getPhoneNumber());
		}
		return userRepository.save(user);
	}
}