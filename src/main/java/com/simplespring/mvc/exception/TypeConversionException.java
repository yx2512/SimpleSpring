package com.simplespring.mvc.exception;

public class TypeConversionException extends RuntimeException{
    static final long serialVersionUID = -603489489111723939L;

    public TypeConversionException() {
    }

    public TypeConversionException(String message) {
        super(message);
    }
}
