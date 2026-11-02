package dev.pgjbz.transactions.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.pgjbz.transactions.domain.exceptions.UnprocessableException;
import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.services.TransactionService;
import dev.pgjbz.transactions.web.controllers.TransactionsController;

@AutoConfigureMockMvc
@WebMvcTest(TransactionsController.class)
class TransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldCreateTransaction() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final Transaction transaction = Instancio.of(Transaction.class).set(Select.field("createdAt"), now).create();
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenReturn(transaction);
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"account_id": 1,"operation_type_id": 1,"amount": 45.5}
                        """)
                .header("transaction-e2e-id", UUID.randomUUID()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(transaction.getId()))
                .andExpect(jsonPath("$.created_at").value(now.toString()));
    }

     @Test 
    void shouldNotCreateTransactionWhenMandatoryHeaderIsNotSent() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(UnprocessableException.withAccountId(123L));
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"account_id": 1,"operation_type_id": 1,"amount": 45.5}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request header 'transaction-e2e-id' for method parameter type UUID is not present"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/transactions"));
    }


    @Test 
    void shouldNotCreateTransactionWhenAccountDoesNotExists() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class)))
                .thenThrow(UnprocessableException.withAccountId(123L));
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"account_id": 1,"operation_type_id": 1,"amount": 45.5}
                        """)
                .header("transaction-e2e-id", UUID.randomUUID()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Cannot process request with account id 123"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.path").value("/transactions"));
    }

    @ParameterizedTest 
    @MethodSource("badRequestPayloadSource")
    void shouldReturnBadRequestWhenMissingFieldOrInvalidValue(final String payload, final String fieldName, final String fieldMessage) throws Exception {
        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("transaction-e2e-id", UUID.randomUUID()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/transactions"))
                .andExpect(jsonPath("$.errors[0].field").value(fieldName))
                .andExpect(jsonPath("$.errors[0].message").value(fieldMessage));
    }


    static Stream<Arguments> badRequestPayloadSource() {
        return Stream.of(
            Arguments.of("{\"operation_type_id\": 1,\"amount\": 45.5}", "accountId", "Account id is mandatory"),
            Arguments.of("{\"account_id\": -1,\"operation_type_id\": 1,\"amount\": 45.5}", "accountId", "Account id must be positive"),
            Arguments.of("{\"account_id\": 1,\"amount\": 45.5}", "operationTypeId", "Operation type is mandatory"),
            Arguments.of("{\"account_id\": 1,\"operation_type_id\": 5,\"amount\": 45.5}", "operationTypeId", "Operation type value must be between 1 and 4"),
            Arguments.of("{\"account_id\": 1,\"operation_type_id\": 0,\"amount\": 45.5}", "operationTypeId", "Operation type value must be between 1 and 4"),
            Arguments.of("{\"account_id\": 1,\"operation_type_id\": 1}", "amount", "Transaction value is mandatory"),
            Arguments.of("{\"account_id\": 1,\"operation_type_id\": 1, \"amount\": -10}", "amount", "Transaction value must be positive"),
            Arguments.of("{\"account_id\": 1,\"operation_type_id\": 1, \"amount\": 0}", "amount", "Transaction value must be positive")
        );
    }
    

}
