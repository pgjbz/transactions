package dev.pgjbz.transactions.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.domain.exceptions.DuplicateAccountException;
import dev.pgjbz.transactions.domain.exceptions.NotFoundException;
import dev.pgjbz.transactions.web.controllers.AccountController;

@AutoConfigureMockMvc
@WebMvcTest(AccountController.class)
class AccountControllerTest {
    private static final String VALID_GENERIC_DOCUMENT = "00000000191";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldCreateAnAccountAndReturnBodyWithHeadersWithLocationAndReturnCreatedHttpStatus() throws Exception {
        when(accountService.createAccount(any(Account.class))).thenReturn(new Account(1L, VALID_GENERIC_DOCUMENT));
        mockMvc.perform(post("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"document_number": "00000000191"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.document_number").value(VALID_GENERIC_DOCUMENT))
            .andExpect(header().string("Location", "http://localhost/v1/accounts/1"));

    }

    @Test
    void shouldNotCreateAnAccountWhenGivenDocumentIsInvalid() throws Exception {
        mockMvc.perform(post("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"document_number": "00000000192"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("invalid request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.path").value("/v1/accounts"))
            .andExpect(jsonPath("$.errors[0].field").value("documentNumber"))
            .andExpect(jsonPath("$.errors[0].message").value("Document must be a valid CPF"));
    }

    @Test
    void shoouldReturnConflictStatusCodeWhenAlreadyExistsAnAccountWithDocument() throws Exception {
        when(accountService.createAccount(any(Account.class))).thenThrow(DuplicateAccountException.withDocumentNumber(VALID_GENERIC_DOCUMENT));

        mockMvc.perform(post("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {"document_number": "00000000191"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("account with document 00000000191 already exists"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.path").value("/v1/accounts"));

    }


    @Test
    void shouldReturnAccountWhenFindById() throws Exception {

        final Account account = new Account(5432L, VALID_GENERIC_DOCUMENT);

        when(accountService.findById(account.getId())).thenReturn(account);

        mockMvc.perform(get("/v1/accounts/5432"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5432))
            .andExpect(jsonPath("document_number").value(VALID_GENERIC_DOCUMENT));

    }

    @Test
    void shouldReturnNotFoundStatusCodeWhenFindUnexistentAccountWhenFindById() throws Exception {
        when(accountService.findById(anyLong())).thenThrow(NotFoundException.accountWithId(5432L));

        mockMvc.perform(get("/v1/accounts/5432"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("account with id 5432 not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/v1/accounts/5432"));

    }
}
