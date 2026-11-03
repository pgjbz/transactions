package dev.pgjbz.transactions.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import dev.pgjbz.transactions.AbstractIntegrationTest;
import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.infra.account.repository.AccountRepository;
import dev.pgjbz.transactions.web.dto.request.AccountCreateRequestDTO;

@Sql("classpath:sqls/clear_accounts.sql")
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AccountIntegrationTest extends AbstractIntegrationTest {
    private static final String INVALID_DOCUMENT = "11477444114";

    private static final String VALID_GENERIC_DOCUMENT = "00000000191";

    @Autowired
    private RestTestClient restTestClient;

    @MockitoSpyBean
    private AccountService accountService;

    @MockitoSpyBean
    private AccountRepository accountRepository;

    @Test
    void shouldCreateAnAccountAndReturnBodyWithHeadersWithLocationAndReturnCreatedHttpStatus() throws Exception {

        final AccountCreateRequestDTO request = new AccountCreateRequestDTO(VALID_GENERIC_DOCUMENT);

        restTestClient.post()
                .uri("/v1/accounts")
                .body(request)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.id").isNumber()
                .jsonPath("$.document_number").isEqualTo(request.documentNumber());

        verify(accountService).createAccount(any(Account.class));
        verify(accountRepository).existsByDocument(VALID_GENERIC_DOCUMENT);
        verify(accountRepository).save(any(Account.class));
        final boolean existsOnDatabase = accountRepository.existsByDocument(VALID_GENERIC_DOCUMENT);
        assertTrue(existsOnDatabase);
    }

    @Test
    void shouldNotCreateAnAccountWhenGivenDocumentIsInvalid() throws Exception {

        final AccountCreateRequestDTO request = new AccountCreateRequestDTO(INVALID_DOCUMENT);

        restTestClient.post()
                .uri("/v1/accounts")
                .body(request)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("invalid request")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.path").isEqualTo("/api/v1/accounts")
                .jsonPath("$.errors[0].field").isEqualTo("documentNumber")
                .jsonPath("$.errors[0].message").isEqualTo("Document must be a valid CPF");

        final boolean existsOnDatabase = accountRepository.existsByDocument(INVALID_DOCUMENT);
        assertFalse(existsOnDatabase);

        verify(accountService, never()).createAccount(any(Account.class));
        verify(accountRepository, never()).existsByDocument(VALID_GENERIC_DOCUMENT);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @Sql("classpath:sqls/create_account.sql")
    void shoouldReturnConflictStatusCodeWhenAlreadyExistsAnAccountWithDocument() throws Exception {

        final AccountCreateRequestDTO request = new AccountCreateRequestDTO(VALID_GENERIC_DOCUMENT);

        restTestClient.post()
                .uri("/v1/accounts")
                .body(request)
                .header("Content-type", "application/json")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("account with document 00000000191 already exists")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.path").isEqualTo("/api/v1/accounts");

        verify(accountService).createAccount(any(Account.class));
        verify(accountRepository).existsByDocument(VALID_GENERIC_DOCUMENT);
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    @Sql("classpath:sqls/create_account_fixed_id.sql")
    void shouldReturnAccountWhenFindById() throws Exception {
        restTestClient.get()
                .uri("/v1/accounts/5432")
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBody()
                .jsonPath("$.id").isEqualTo(5432)
                .jsonPath("$.document_number").isEqualTo("00000000191");

        verify(accountService).findById(5432L);
        verify(accountRepository, never()).existsByDocument(anyString());
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    void shouldReturnNotFoundStatusCodeWhenFindUnexistentAccountWhenFindById() throws Exception {
        restTestClient.get()
                .uri("/v1/accounts/55547")
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/api/v1/accounts/55547")
                .jsonPath("$.message").isEqualTo("account with id 55547 not found")
                .jsonPath("$.status").isEqualTo(404);

        verify(accountService).findById(55547L);
        verify(accountRepository, never()).existsByDocument(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }
}
