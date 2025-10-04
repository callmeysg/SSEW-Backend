package com.singhtwenty2.commerce_service.util.sanitizer;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SpecificationSanitizer {

    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s()/-]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern CONSECUTIVE_UNDERSCORES_PATTERN = Pattern.compile("_{2,}");

    public Map<String, String> sanitizeSpecifications(Map<String, String> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> sanitizedSpecs = new HashMap<>();

        for (Map.Entry<String, String> entry : specifications.entrySet()) {
            String originalKey = entry.getKey();
            String value = entry.getValue();

            if (!StringUtils.hasText(originalKey) || !StringUtils.hasText(value)) {
                continue;
            }

            String sanitizedKey = sanitizeAndConvertToSnakeCase(originalKey.trim());
            String sanitizedValue = sanitizeValue(value.trim());

            if (StringUtils.hasText(sanitizedKey) && StringUtils.hasText(sanitizedValue)) {
                sanitizedSpecs.put(sanitizedKey, sanitizedValue);
            }
        }

        return sanitizedSpecs;
    }

    private String sanitizeAndConvertToSnakeCase(String key) {
        String cleaned = SPECIAL_CHARS_PATTERN.matcher(key).replaceAll(" ");
        cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll("_");
        cleaned = cleaned.toLowerCase();
        cleaned = CONSECUTIVE_UNDERSCORES_PATTERN.matcher(cleaned).replaceAll("_");
        cleaned = cleaned.replaceAll("^_+|_+$", "");

        return cleaned;
    }

    private String sanitizeValue(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
