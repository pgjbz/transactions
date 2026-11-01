package dev.pgjbz.transactions.web.controller;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import dev.pgjbz.transactions.AbstractIntegrationTest;
import dev.pgjbz.transactions.web.dto.request.AccountCreateRequestDTO;

@Sql("classpath:sqls/clear_accounts.sql")
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AccountControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String VALID_GENERIC_DOCUMENT = "00000000191";

    @Autowired
    private RestTestClient restTestClient;

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
        
    }

    @Test
    void shouldNotCreateAnAccountWhenGivenDocumentIsInvalid() throws Exception {

        final AccountCreateRequestDTO request = new AccountCreateRequestDTO("11477444114");

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
            .jsonPath("$.message").isEqualTo("Conflict")
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.path").isEqualTo("/api/v1/accounts");
        // when(accountService.createAccount(any(Account.class))).thenThrow(DuplicateKeyException.class);

        // mockMvc.perform(post("/v1/accounts")
        // .contentType(MediaType.APPLICATION_JSON)
        // .content("""
        // {"document_number": "00000000191"}
        // """))
        // .andExpect(status().isConflict())
        // .andExpect(jsonPath("$.message").value("Conflict"))
        // .andExpect(jsonPath("$.status").value(409))
        // .andExpect(jsonPath("$.path").value("/v1/accounts"));

    }
}
