package com.singhtwenty2.commerce_service.data.validation;

import com.singhtwenty2.commerce_service.data.enums.OrderStatus;
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