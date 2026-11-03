package dev.pgjbz.transactions.web.controllers;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.services.TransactionService;
import dev.pgjbz.transactions.web.dto.request.TransactionCreateRequestDTO;
import dev.pgjbz.transactions.web.dto.response.TransactionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/transactions")
@Tag(name = "transactions", description = "Transaction Operations")
public class TransactionController {

    private final TransactionService transactionService;


    @Operation(summary = "Create new transaction for existent account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Found the account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponseDTO.class)) }),
            @ApiResponse(responseCode = "409", description = "Duplicate transaction", content = @Content),
            @ApiResponse(responseCode = "422", description = "Invalid account id", content = @Content),
         })
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestBody @Valid final TransactionCreateRequestDTO transactionCreateRequestDTO,
            @Schema(name = "transaction-e2e-id", description = "unique transaction id", requiredMode = REQUIRED) 
            @RequestHeader(name = "transaction-e2e-id", required = true) 
            final UUID e2eId) {
        final Transaction transaction = new Transaction(transactionCreateRequestDTO.accountId(),
                transactionCreateRequestDTO.operationTypeId(), transactionCreateRequestDTO.amount(), e2eId);
        final Transaction transactionCreated = transactionService.createTransaction(transaction);
        return ResponseEntity.status(CREATED)
                .body(new TransactionResponseDTO(transactionCreated.getId(), transactionCreated.getCreatedAt()));
    }
}
