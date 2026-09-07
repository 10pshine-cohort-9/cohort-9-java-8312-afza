package com.contactmanager.backend.exception;

public class RegistrationPersistenceException extends RuntimeException {
    public RegistrationPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
