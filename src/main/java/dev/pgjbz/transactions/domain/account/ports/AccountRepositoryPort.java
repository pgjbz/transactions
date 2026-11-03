package dev.pgjbz.transactions.domain.account.ports;

import java.util.Optional;

import dev.pgjbz.transactions.domain.account.models.Account;

public interface AccountRepositoryPort {
    
    Account save(final Account account);
    Optional<Account> findById(final Long id);
    boolean existsByDocument(final String documentNumber);

}
