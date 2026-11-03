package dev.pgjbz.transactions.infra.transaction.repository;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import dev.pgjbz.transactions.domain.exceptions.DuplicateTransactionException;
import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.ports.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TransactionRepository implements TransactionRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public Transaction save(final Transaction transaction) {
        try {
            final KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcClient.sql("""
                    insert into transactions(account_id, operation_type, amount, event_date, transaction_e2e_id)
                    values (:accountId, :operationType, :amount, :eventDate, :e2eId)
                    returning id
                    """)
                    .param("accountId", transaction.getAccountId())
                    .param("operationType", transaction.getOperationType().getInternalId())
                    .param("amount", transaction.getAmount())
                    .param("eventDate", transaction.getCreatedAt())
                    .param("e2eId", transaction.getTransactionE2eId())
                    .update(keyHolder);
            return transaction.withId(keyHolder.getKeyAs(Long.class));
        } catch (final DuplicateKeyException ex) {
            throw DuplicateTransactionException.withE2eId(transaction.getTransactionE2eId());
        }
    }

    @Override
    @Cacheable(cacheNames = "transactions",unless = "#result == false", key = "'transaction_exists_e2e' + #e2eId")
    public boolean existsByE2eId(final UUID e2eId) {
        return jdbcClient.sql(
                """
                        select
                            1
                        from
                            transactions
                        where
                            transaction_e2e_id = :e2eId
                        """)
                .param("e2eId", e2eId)
                .query(Long.class)
                .optional().isPresent();
    }

}
