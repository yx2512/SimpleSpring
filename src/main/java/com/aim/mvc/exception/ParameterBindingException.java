package com.aim.mvc.exception;

public class ParameterBindingException extends RuntimeException{
    static final long serialVersionUID = 5034894898045723939L;

    public ParameterBindingException() {
    }

    public ParameterBindingException(String message) {
        super(message);
    }
}
