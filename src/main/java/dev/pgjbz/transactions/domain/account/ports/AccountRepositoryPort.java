package dev.pgjbz.transactions.domain.account.ports;

import dev.pgjbz.transactions.domain.account.models.Account;

public interface AccountRepositoryPort {
    
    Account save(final Account account);
    Account findById(final Long id);
    boolean existsByDocument(final String documentNumber);

}
