package com.aim.core.exception;

public class DependencyInjectionException extends RuntimeException{
    static final long serialVersionUID = -3235532434985766939L;

    public DependencyInjectionException() {
    }

    public DependencyInjectionException(String message) {
        super(message);
    }
}
