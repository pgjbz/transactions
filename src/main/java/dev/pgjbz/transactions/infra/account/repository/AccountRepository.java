package dev.pgjbz.transactions.infra.account.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.ports.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccountRepository implements AccountRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public Account save(final Account account) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        final String document = account.getDocument();
        jdbcClient.sql("""
                    insert into accounts(document_number)
                    values (:documentNumber)
                    returning id
                """)
                .param("documentNumber", document)
                .update(keyHolder);
        return new Account(keyHolder.getKeyAs(Long.class), document);
    }

}
