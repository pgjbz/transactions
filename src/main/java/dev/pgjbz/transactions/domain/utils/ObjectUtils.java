package dev.pgjbz.transactions.domain.utils;

import java.util.Objects;

import dev.pgjbz.transactions.domain.exceptions.InvalidValueException;

public class ObjectUtils {
    private ObjectUtils() throws Exception {
        throw new IllegalAccessException("utility class");
    }

    public static <T> T required(final T input, final String nullMessage) {
        if(Objects.isNull(input)) {
            throw new InvalidValueException(nullMessage);
        }
        return input;
    }

    public static <T extends Number> T positive(final T input, final String negativeMessage) {
        final T inputNonNull = required(input, negativeMessage);
        if(inputNonNull.doubleValue() < 0) {
            throw new InvalidValueException(negativeMessage);
        }
        return inputNonNull;
    }
}
