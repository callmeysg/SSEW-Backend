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
package com.singhtwenty2.ssew_core.data.validation;

import com.singhtwenty2.ssew_core.data.enums.OrderStatus;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidOrderStatus.ValidOrderStatusValidator.class)
@Documented
public @interface ValidOrderStatus {

    String message() default "Invalid order status. CANCELLED status is not allowed for status updates.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class ValidOrderStatusValidator implements ConstraintValidator<ValidOrderStatus, OrderStatus> {

        @Override
        public void initialize(ValidOrderStatus constraintAnnotation) {
        }

        @Override
        public boolean isValid(OrderStatus value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            return value != OrderStatus.CANCELLED;
        }
    }
}