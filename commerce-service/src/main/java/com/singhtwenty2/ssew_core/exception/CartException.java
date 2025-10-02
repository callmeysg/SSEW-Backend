/**
 * Copyright 2025 SSEW Core Service
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
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