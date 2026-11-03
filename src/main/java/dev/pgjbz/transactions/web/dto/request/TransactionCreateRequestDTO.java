package dev.pgjbz.transactions.web.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionCreateRequestDTO(
        @Schema(description = "Valid and existent account id", example = "5432")
        @NotNull(message = "{transaction.account.id.null}") 
        @Positive(message = "{transaction.account.id.negative}") 
        Long accountId,
        @Schema(description = "Operation type id, valid values 1(Normal Purchase), 2(Purchase with installments), 3(Withdraw), 4(Credit voucher)", example = "1")
        @NotNull(message = "{operationtype.required}") 
        @Min(value = 1, message = "{operationtype.value.range}") 
        @Max(value = 4, message = "{operationtype.value.range}")
        Integer operationTypeId,
        @Schema(description = "Positive non zero amount of transaction", example = "45.74")
        @NotNull(message = "{transaction.amount.required}") 
        @Positive(message = "{transaction.amount.positive}") 
        BigDecimal amount) {
}
