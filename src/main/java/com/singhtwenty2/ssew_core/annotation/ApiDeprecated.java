package com.singhtwenty2.ssew_core.annotation;

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