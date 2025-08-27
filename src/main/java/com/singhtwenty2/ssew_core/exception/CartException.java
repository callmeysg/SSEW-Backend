package com.singhtwenty2.ssew_core.exception;

public class CartException extends BusinessException {

    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class CartNotFound extends CartException {
        public CartNotFound(String message) {
            super(message);
        }
    }

    public static class CartItemNotFound extends CartException {
        public CartItemNotFound(String message) {
            super(message);
        }
    }

    public static class InvalidCartOperation extends CartException {
        public InvalidCartOperation(String message) {
            super(message);
        }
    }

    public static class CartItemAlreadyExists extends CartException {
        public CartItemAlreadyExists(String message) {
            super(message);
        }
    }
}