package dev.pgjbz.transactions.domain.transaction.ports;

import java.util.UUID;

import dev.pgjbz.transactions.domain.transaction.models.Transaction;

public interface TransactionRepositoryPort {
    Transaction save(final Transaction transaction);
    boolean existsByE2eId(final UUID e2eId);
}
