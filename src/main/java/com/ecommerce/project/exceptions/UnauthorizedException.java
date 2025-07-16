package com.ecommerce.project.exceptions;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Full authentication is required to access this resource");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
