package com.singhtwenty2.ssew_core.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");

    public String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "product";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = withoutAccents.toLowerCase(Locale.ENGLISH);
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        slug = EDGE_DASHES.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-");

        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
            slug = EDGE_DASHES.matcher(slug).replaceAll("");
        }

        return slug.isEmpty() ? "product" : slug;
    }
}