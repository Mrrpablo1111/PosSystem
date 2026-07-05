package com.sh.sh.pos.system.service.userservice;

import java.util.List;
import java.util.Set;

import com.sh.sh.pos.system.domain.UserRole;
import com.sh.sh.pos.system.model.user.User;
import com.sh.sh.pos.system.payload.request.UpdateUserDto;

public interface UserService {

	User getUserFromJwtToken(String jwt);

	User getCurrentUser();

	User getUserByEmail(String email);

	User getUserById(Long id);

	Set<User> getUserByRole(UserRole role);

	List<User> getAllUser();

	User updateUser(UpdateUserDto updateData, User user);
}