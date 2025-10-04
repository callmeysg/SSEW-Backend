package com.singhtwenty2.commerce_service.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncoderService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String input) {
        return encoder.encode(input);
    }

    public boolean matches(String input, String encodedValue) {
        return encoder.matches(input, encodedValue);
    }
}
