package dev.pgjbz.transactions.domain.transaction.services;

import org.slf4j.MDC;

import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.domain.exceptions.BusinessException;
import dev.pgjbz.transactions.domain.exceptions.DuplicateTransactionException;
import dev.pgjbz.transactions.domain.exceptions.NotFoundException;
import dev.pgjbz.transactions.domain.exceptions.UnprocessableException;
import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.ports.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountService accountService;

    public Transaction createTransaction(final Transaction transaction) {
        try {
            MDC.put("transaction_account_id", String.valueOf(transaction.getAccountId()));
            MDC.put("transaction_amount", String.valueOf(transaction.getAmount()));
            MDC.put("transaction_e2e_id", transaction.getTransactionE2eId().toString());
            if (transactionRepository.existsByE2eId(transaction.getTransactionE2eId())) {
                throw DuplicateTransactionException.withE2eId(transaction.getTransactionE2eId());
            }
            accountService.findById(transaction.getAccountId());
            return transactionRepository.save(transaction);
        } catch (final NotFoundException ex) {
            log.warn("{}", ex.getMessage());
            throw UnprocessableException.withAccountId(transaction.getAccountId());
        } catch (final BusinessException ex) {
            log.warn("{}", ex.getMessage(), ex);
            throw ex;
        } catch (final Exception ex) {
            log.error("error when query database: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }
}
