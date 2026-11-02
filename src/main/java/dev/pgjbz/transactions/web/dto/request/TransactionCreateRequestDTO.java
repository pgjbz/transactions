package dev.pgjbz.transactions.web.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionCreateRequestDTO(
        @NotNull(message = "{transaction.account.id.null}") 
        @Positive(message = "{transaction.account.id.negative}") 
        Long accountId,
        @NotNull(message = "{operationtype.required}") 
        @Min(value = 1, message = "{operationtype.value.range}") 
        @Max(value = 4, message = "{operationtype.value.range}") 
        Integer operationTypeId,
        @NotNull(message = "{transaction.amount.required}") 
        @Positive(message = "{transaction.amount.positive}") 
        BigDecimal amount) {
}
