package dev.pgjbz.transactions.domain.utils;

import static java.util.Objects.isNull;

import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern NORMALIZE_DOCUMENT_PATTERN = Pattern.compile("[./-]");

    private StringUtils() throws Exception {
        throw new IllegalAccessException("utility class");
    }


    public static String normalizeDocument(final String input) {
        if(isNull(input) || input.isBlank()) {
            return input;
        }

        return NORMALIZE_DOCUMENT_PATTERN.matcher(input).replaceAll("");
    }
}
