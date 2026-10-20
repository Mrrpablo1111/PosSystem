
package com.sh.sh.pos.system.validator;

import org.springframework.stereotype.Component;

import com.sh.sh.pos.system.exception.UserException;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public void validate(String password) {

        if (password == null || password.isBlank()) {
            throw new UserException("Password is required");
        }

        if (password.length() < MIN_LENGTH) {
            throw new UserException(
                    "Password must be at least 8 characters"
            );
        }

        if (password.contains(" ")) {
            throw new UserException(
                    "Password must not contain spaces"
            );
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new UserException(
                    "Password must contain at least one uppercase letter"
            );
        }

        if (!password.matches(".*[a-z].*")) {
            throw new UserException(
                    "Password must contain at least one lowercase letter"
            );
        }

        if (!password.matches(".*\\d.*")) {
            throw new UserException(
                    "Password must contain at least one number"
            );
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            throw new UserException(
                    "Password must contain at least one special character"
            );
        }
    }

    public boolean isStrong(String password) {
        try {
            validate(password);
            return true;
        } catch (UserException e) {
            return false;
        }
    }
}
