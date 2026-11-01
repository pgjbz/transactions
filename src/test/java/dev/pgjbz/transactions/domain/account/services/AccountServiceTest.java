package dev.pgjbz.transactions.domain.account.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.ports.AccountRepositoryPort;

@ExtendWith({ MockitoExtension.class, OutputCaptureExtension.class })
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepositoryPort accountPort;

    @Test
    void givenAccountCreateANewAccountWithoutErrors(final CapturedOutput output) {
        final Account account = Instancio.create(Account.class);

        when(accountPort.save(account)).thenReturn(account);

        assertDoesNotThrow(() -> accountService.createAccount(account));

        assertTrue(output.getOut().contains("creating new account"));
    }

    @Test
    void shouldThrowAnExceptionWhenRepositoryPortThrowsAnException(final CapturedOutput output) {
        final Account account = Instancio.create(Account.class);
        when(accountPort.save(account)).thenThrow(RuntimeException.class);

        assertThrows(Exception.class, () -> accountService.createAccount(account));

        assertTrue(output.getOut().contains("creating new account"));
        assertTrue(output.getOut().contains("error when create an account"));

    }
}
