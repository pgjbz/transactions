package dev.pgjbz.transactions.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringUtilsTest {

    @ParameterizedTest
    @MethodSource("formatedDocumentSource") 
    void shouldRemoveNonNumbersFromDocument(final String given, final String expected) {
        final String result = StringUtils.normalizeDocument(given);
        assertEquals(expected, result, "expected " + expected + "but got " + result);
    }

    static Stream<Arguments> formatedDocumentSource() {
        return Stream.of(
            Arguments.of("000.000.001-91", "00000000191"),
            Arguments.of("73.983.984/0001-41", "73983984000141"),
            Arguments.of("R0.24A.2FA/0001-50", "R024A2FA000150")
        );
    }

}
