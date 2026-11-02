package dev.pgjbz.transactions.domain.exceptions;

public class NotFoundException extends BusinessException {

    private static final String ACCOUNT_NOT_FIND_MESSAGE = "account with id %s not found";

    private NotFoundException(final String message) {
        super(message);
    }

    public static NotFoundException accountWithId(final Long id) {
        return new NotFoundException(ACCOUNT_NOT_FIND_MESSAGE.formatted(id));
    }
}
