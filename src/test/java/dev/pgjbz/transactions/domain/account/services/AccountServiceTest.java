package dev.pgjbz.transactions.domain.account.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import dev.pgjbz.transactions.domain.exceptions.DuplicateAccountException;
import dev.pgjbz.transactions.domain.exceptions.NotFoundException;

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

        verify(accountPort).existsByDocument(anyString());
        verify(accountPort).save(account);
    }

    @Test
    void shouldThrowAnExceptionWhenRepositoryPortThrowsAnException(final CapturedOutput output) {
        final Account account = Instancio.create(Account.class);
        when(accountPort.save(account)).thenThrow(DuplicateAccountException.class);

        assertThrows(DuplicateAccountException.class, () -> accountService.createAccount(account));

        assertTrue(output.getOut().contains("creating new account"));
        assertTrue(output.getOut().contains("error when create an account"));

        verify(accountPort).existsByDocument(anyString());
        verify(accountPort).save(account);
    }

     @Test
    void shouldThrowAnExceptionWhenAccountAlreadyExists(final CapturedOutput output) {
        final Account account = Instancio.create(Account.class);
        when(accountPort.existsByDocument(anyString())).thenReturn(true);

        assertThrows(DuplicateAccountException.class, () -> accountService.createAccount(account));

        assertTrue(output.getOut().contains("creating new account"));
        assertTrue(output.getOut().contains("error when create an account"));

        verify(accountPort).existsByDocument(anyString());
        verify(accountPort, never()).save(account);
    }

    @Test 
    void shouldFindAccountWhenSearchById(final CapturedOutput output) {
        final Account account = Instancio.create(Account.class);
        when(accountPort.findById(anyLong())).thenReturn(account);
        assertDoesNotThrow(() -> accountService.findById(1L));

        assertTrue(output.getOut().contains("fetch account"));
        verify(accountPort).findById(anyLong());
    }

    @Test 
    void shouldThrowNotFoundWhenFindByIdAndNotExists() {

        when(accountPort.findById(anyLong())).thenThrow(NotFoundException.accountWithId(123L));
        assertThrows(NotFoundException.class, () -> accountService.findById(1L));

        verify(accountPort).findById(anyLong());
        
    }
}
