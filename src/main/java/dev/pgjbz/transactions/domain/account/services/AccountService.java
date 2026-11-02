package dev.pgjbz.transactions.domain.account.services;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.ports.AccountRepositoryPort;
import dev.pgjbz.transactions.domain.exceptions.BusinessException;
import dev.pgjbz.transactions.domain.exceptions.DuplicateAccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepositoryPort accountPort;

    public Account createAccount(final Account account) {
        try {
            final String document = account.getDocument();
            MDC.put("account_document", document);
            log.info("creating new account");
            if (accountPort.existsByDocument(document)) {
                throw DuplicateAccountException.withDocumentNumber(document);
            }
            return accountPort.save(account);
        } catch (final Exception ex) {
            log.warn("error when create an account: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }

    public Account findById(final Long id) {
        try {
            MDC.put("account_id", String.valueOf(id));
            log.info("fetch account");
            return accountPort.findById(id);
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
