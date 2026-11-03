package dev.pgjbz.transactions.domain.transaction.models;

import static dev.pgjbz.transactions.domain.utils.ObjectUtils.required;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.pgjbz.transactions.domain.transaction.models.enums.OperationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class Transaction {

    @With 
    private final Long id;
    private final Long accountId;
    private final OperationType operationType;
    private final BigDecimal amount;
    private final UUID transactionE2eId;
    private final LocalDateTime createdAt;

    public Transaction(final Long accountId, final Integer operationTypeId, final BigDecimal amount, final UUID transactionE2eId) {
        this.id = null;
        this.accountId = required(accountId, "Account id is mandatory");
        this.operationType = OperationType.fromInternalId(required(operationTypeId, "Operation type id is mandatory"));
        this.amount = this.operationType.normalizeAmount(required(amount, "Amount is required"));
        this.transactionE2eId = required(transactionE2eId, "Transaction e2e id is mandatory");
        this.createdAt = LocalDateTime.now();
    }

}
