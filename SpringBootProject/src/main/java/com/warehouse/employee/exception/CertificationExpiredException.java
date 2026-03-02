package com.warehouse.employee.exception;

public class CertificationExpiredException extends RuntimeException {
    public CertificationExpiredException(String message) {
        super(message);
    }
}