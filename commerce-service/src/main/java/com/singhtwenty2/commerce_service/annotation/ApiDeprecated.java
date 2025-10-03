package com.singhtwenty2.commerce_service.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDeprecated {
    String since() default "";

    String replacement() default "";

    String sunsetDate() default "";

    String message() default "This endpoint is deprecated";
}