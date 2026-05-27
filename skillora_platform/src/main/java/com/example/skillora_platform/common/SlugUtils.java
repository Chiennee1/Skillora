package com.example.skillora_platform.common;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugUtils {

    private static final String DEFAULT_SLUG = "item";

    private SlugUtils() {
    }

    public static String toSlug(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_SLUG;
        }

        String normalized = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replace("đ", "d")
                .replace("Đ", "d")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");

        return normalized.isBlank() ? DEFAULT_SLUG : normalized;
    }
}
