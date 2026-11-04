package dev.pgjbz.transactions.infra.account.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.ports.AccountRepositoryPort;
import dev.pgjbz.transactions.domain.exceptions.DuplicateAccountException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AccountRepository implements AccountRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public Account save(final Account account) {
        try {
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
        } catch (final DuplicateKeyException _) {
            throw DuplicateAccountException.withDocumentNumber(account.getDocument());
        }
    }

    @Override
    @Cacheable(cacheNames = "accounts", unless = "#result == null", key = "'account:' + #id")
    public Optional<Account> findById(final Long id) {
        return jdbcClient.sql("""
                select
                    id,
                    document_number
                from
                    accounts
                where
                    id = :id
                            """).param("id", id)
                .query((rs, rowNum) -> new Account(rs.getLong("id"), rs.getString("document_number")))
                .optional();
    }

    @Override
    @Cacheable(cacheNames = "accounts", unless = "#result == false", key = "'account_document_exists:' + #documentNumber")
    public boolean existsByDocument(final String documentNumber) {
        return jdbcClient.sql("""
                    select
                        1
                    from
                        accounts
                    where
                        document_number = :documentNumber
                """).param("documentNumber", documentNumber)
                .query(Long.class)
                .optional()
                .isPresent();
    }

}
