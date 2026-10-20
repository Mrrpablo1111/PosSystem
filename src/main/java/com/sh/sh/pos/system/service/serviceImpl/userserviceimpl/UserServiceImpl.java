package com.sh.sh.pos.system.service.serviceimpl.userserviceimpl;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	@Transactional(readOnly = true)
	public User getUserFromJwtToken(String jwt) {
		
		if(jwt == null || jwt.isBlank()){
			throw new UserException("JWT token required");
		}
		String email = jwtProvider.getEmailFromToken(jwt);

		if (email == null || email.isBlank()) {
			throw new UserException("Invalid token");
		}
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid token"));
	}

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUser() {
		Authentication authentication =
				SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
            throw new UserException(
                    "No authenticated user in security context"
            );
        }

		if (!authentication.isAuthenticated()) {
            throw new UserException(
                    "User is not authenticated"
            );
        }

		String email = authentication.getName();
		if(email == null || email.isBlank()){
			throw new UserException("No authenticated user in securitry context");
		}

		log.debug("Current authentication: name={} authorities={}",
				email, authentication.getAuthorities());

		return userRepository.findByEmail(email).orElseThrow(()-> new UserException(USER_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserByEmail(String email) {
		if(email == null || email.isBlank()){
			throw new UserException("Email is required");
		}
		return userRepository.findByEmail(email).orElseThrow(()-> new UserException(USER_NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public User getUserById(Long id) {
		if (id == null) {
            throw new ResourceNotFoundException("User ID is required");
        }

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
	}

	@Override
	@Transactional (readOnly = true)
	public List<User> getAllUser() {
		return userRepository.findAll();
	}

	@Override
	@Transactional (readOnly = true)
	public Set<User> getUserByRole(UserRole role) {
		if (role == null) {
            throw new UserException("User role is required");
        }

        return userRepository.findByRole(role);
	}

	@Override
	public User updateUser(UpdateUserDto updateData, User user) {
		 if (updateData == null) {
            throw new UserException("Update data is required");
        }

        if (user == null) {
            throw new UserException(USER_NOT_FOUND);
        }

        if (updateData.getFullName() != null
                && !updateData.getFullName().isBlank()) {

            user.setFullName(
                    updateData.getFullName().trim()
            );
        }

        if (updateData.getPhoneNumber() != null
                && !updateData.getPhoneNumber().isBlank()) {

            user.setPhone(
                    updateData.getPhoneNumber().trim()
            );
        }

        return userRepository.save(user);
	}
}