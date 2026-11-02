package dev.pgjbz.transactions.domain.exceptions;

public class BusinessException extends RuntimeException {
    protected BusinessException(final String message) {
        super(message);
    }
    
}
