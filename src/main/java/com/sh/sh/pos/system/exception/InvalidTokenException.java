package com.sh.sh.pos.system.exception;

/**
 * Thrown when a JWT token is missing, malformed, or invalid.
 * Replaces generic RuntimeException in JwtProvider.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}