package com.aim.core.exception;

public class BeanRegistrationException extends RuntimeException{
    static final long serialVersionUID = -6034894898045766939L;

    public BeanRegistrationException() {
    }

    public BeanRegistrationException(String message) {
        super(message);
    }
}
