package dev.pgjbz.transactions.web.controllers;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/transactions")
public class TransactionsController {

    private final TransactionService transactionService;

    @PostMapping 
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestBody @Valid final TransactionCreateRequestDTO transactionCreateRequestDTO,
            @RequestHeader(name = "transaction-e2e-id", required = true) final UUID e2eId) {
        final Transaction transaction = new Transaction(transactionCreateRequestDTO.accountId(),
                transactionCreateRequestDTO.operationTypeId(), transactionCreateRequestDTO.amount(), e2eId);
        final Transaction transactionCreated = transactionService.createTransaction(transaction);
        return ResponseEntity.status(CREATED)
                .body(new TransactionResponseDTO(transactionCreated.getId(), transactionCreated.getCreatedAt()));
    }
}
