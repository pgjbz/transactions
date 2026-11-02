package dev.pgjbz.transactions.domain.transaction.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.domain.exceptions.DuplicateTransactionException;
import dev.pgjbz.transactions.domain.exceptions.NotFoundException;
import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.ports.TransactionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private AccountService accountService;

    @Test
    void shouldCreateATransactionWithoutErrors() {
        final Transaction transaction = Instancio.create(Transaction.class);

        assertDoesNotThrow(() -> transactionService.createTransaction(transaction));
    }

    @Test
    void shouldThrowsDuplicateTransactionExceptionWhenTransactionAlreadyExists() {
        final Transaction transaction = Instancio.create(Transaction.class);

        when(transactionRepository.existsByE2eId(any(UUID.class))).thenReturn(true);

        assertThrows(DuplicateTransactionException.class, () -> transactionService.createTransaction(transaction));
    }

    @Test
    void shouldThrowsNotFoundWhenTryToCreateTransactionWithUnexistentAccount() {
        final Transaction transaction = Instancio.create(Transaction.class);

        when(accountService.findById(anyLong())).thenThrow(NotFoundException.accountWithId(123L));

        assertThrows(NotFoundException.class, () -> transactionService.createTransaction(transaction));
    }

    @Test 
    void shouldRethrowExceptionWhenExceptionWasThrow() {
        final Transaction transaction = Instancio.create(Transaction.class);
        
        when(transactionRepository.save(transaction)).thenThrow(RuntimeException.class);

        assertThrows(Exception.class, () -> transactionService.createTransaction(transaction));
    }

}
