package com.sh.sh.pos.system.service;

import java.util.List;

import com.sh.sh.pos.system.exceptions.UserException;
import com.sh.sh.pos.system.model.User;

public interface UserService {
	User getUserFromJwtToken(String jwt) throws UserException;
	User getCurrentUser() throws UserException;
	User getUserByEmail(String email) throws UserException;
	User getUserById(Long id);
	List<User> getAllUser();
	
}
