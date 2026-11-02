package dev.pgjbz.transactions.domain.exceptions;

public class UnprocessableException extends BusinessException {

    private UnprocessableException(String message) {
        super(message);
    }

    public static UnprocessableException withAccountId(final Long id) {
        return new UnprocessableException("Cannot process request with account id %s".formatted(id));
    }
    
}
