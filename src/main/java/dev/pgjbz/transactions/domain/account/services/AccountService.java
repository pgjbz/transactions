package dev.pgjbz.transactions.domain.account.services;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.ports.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepositoryPort accountPort;

    public Account createAccount(final Account account) {
        MDC.put("account_document", account.getDocument());
        try {
            log.info("creating new account");
            return accountPort.save(account);
        } catch (final Exception ex) {
            log.warn("error when create an account: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }

}
