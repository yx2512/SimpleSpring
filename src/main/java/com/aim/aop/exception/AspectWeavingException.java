package com.aim.aop.exception;

public class AspectWeavingException extends RuntimeException{
    static final long serialVersionUID = 3489489804555723939L;

    public AspectWeavingException() {
    }

    public AspectWeavingException(String message) {
        super(message);
    }
}
