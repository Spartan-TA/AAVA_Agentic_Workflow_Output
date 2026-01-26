package com.company.warehouse.common;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
