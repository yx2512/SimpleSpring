package com.simplespring.mvc.exception;

public class BadRequestException extends RuntimeException{
    static final long serialVersionUID = -6034894898045723939L;

    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }
}
