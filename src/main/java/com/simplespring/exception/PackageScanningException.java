package com.simplespring.exception;

public class PackageScanningException extends RuntimeException{
    static final long serialVersionUID = -3235532434345766939L;

    public PackageScanningException() {
    }

    public PackageScanningException(String message) {
        super(message);
    }
}
