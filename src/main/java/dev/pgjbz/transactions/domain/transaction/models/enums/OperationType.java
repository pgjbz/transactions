package dev.pgjbz.transactions.domain.transaction.models.enums;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PRIVATE)
public enum OperationType {
    NORMAL_PURCHASE(1),
    PURCHASE_WITH_INSTALLMENTS(2),
    WITHDRAWAL(3),
    CREDIT_VOUCHER(4);

    @Getter
    private final Integer internalId;

    public static OperationType fromInternalId(Integer operationTypeId) {
        for(final OperationType operation: values()) {
            if(operation.internalId == operationTypeId) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Operation with ID %d does not exists".formatted(operationTypeId));
    }

    public BigDecimal normalizeAmount(final BigDecimal input) {
        if(isNull(input)) {
            return input;
        }
        return switch(this) {
            case NORMAL_PURCHASE, PURCHASE_WITH_INSTALLMENTS, WITHDRAWAL -> input.abs().negate();
            case CREDIT_VOUCHER -> input.abs();
        };
    }

}
