package dev.pgjbz.transactions.domain.exceptions;

import java.util.UUID;

public class DuplicateTransactionException extends BusinessException {

    private static final String MESSAGE = "Transaction with e2e id %s already exists";

    public DuplicateTransactionException(final String message) {
        super(message);
    }

    public static DuplicateTransactionException withE2eId(final UUID e2eId) {
        return new DuplicateTransactionException(MESSAGE.formatted(e2eId));
    }
    
}
