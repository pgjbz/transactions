package dev.pgjbz.transactions.domain.exceptions;

public class DuplicateAccountException extends BusinessException {

    private DuplicateAccountException(String message) {
        super(message);
    }

    public static DuplicateAccountException withDocumentNumber(final String document) {
        return new DuplicateAccountException("account with document %s already exists".formatted(document));
    }
    
}
