package dev.pgjbz.transactions.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;

import dev.pgjbz.transactions.AbstractIntegrationTest;
import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.domain.transaction.models.Transaction;
import dev.pgjbz.transactions.domain.transaction.models.enums.OperationType;
import dev.pgjbz.transactions.domain.transaction.services.TransactionService;
import dev.pgjbz.transactions.infra.transaction.repository.TransactionRepository;
import dev.pgjbz.transactions.web.dto.request.TransactionCreateRequestDTO;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"classpath:sqls/clear_transactions.sql", "classpath:sqls/clear_accounts.sql", "classpath:sqls/create_account_fixed_id.sql"})
class TransactionIntegrationTest extends AbstractIntegrationTest {

    private static final String TRANSACTIONS_BASE_PATH = "/transactions";

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private RestTestClient restTestClient;

    @MockitoSpyBean
    private TransactionService transactionService;

    @MockitoSpyBean
    private TransactionRepository transactionRepository;

    @MockitoSpyBean
    private AccountService accountService;

    @ParameterizedTest
    @MethodSource("validTransactionsWithDifferentOperations")
    void shouldCreateAnTransaction(final TransactionCreateRequestDTO request, final OperationType expectedOperationType,
            final BigDecimal expectedAmount) {

        final UUID transactionE2eId = UUID.randomUUID();
        restTestClient.post()
                .uri(TRANSACTIONS_BASE_PATH)
                .body(request)
                .header("Content-type", "application/json")
                .header("transaction-e2e-id", transactionE2eId.toString())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.created_at").exists();

        verify(transactionService).createTransaction(any(Transaction.class));
        verify(transactionRepository).existsByE2eId(any(UUID.class));
        verify(transactionRepository).save(any(Transaction.class));

        final List<Transaction> transactions = findTransactions();
        final Transaction transaction = transactions.get(0);
        assertEquals(transactionE2eId, transaction.getTransactionE2eId());
        assertEquals(expectedAmount, transaction.getAmount());
        assertEquals(expectedOperationType, transaction.getOperationType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void shouldNotCreateATransactionWhenSendUnexistentAccount() {
        final TransactionCreateRequestDTO request = new TransactionCreateRequestDTO(3306L, 1, BigDecimal.TEN);

        final UUID transactionE2eId = UUID.randomUUID();

        restTestClient.post()
                .uri(TRANSACTIONS_BASE_PATH)
                .body(request)
                .header("Content-type", "application/json")
                .header("transaction-e2e-id", transactionE2eId.toString())
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.message").isEqualTo("Cannot process request with account id 3306")
                .jsonPath("$.path").isEqualTo(TRANSACTIONS_BASE_PATH);

        verify(transactionService).createTransaction(any(Transaction.class));
        verify(transactionRepository).existsByE2eId(any(UUID.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountService).findById(3306L);
    }

    @Test
    @Sql({"classpath:sqls/create_account_fixed_id.sql", "classpath:sqls/create_transaction.sql"})
    void shouldNotCreateAnAccountWhenGivenExistentE2eId() {

        final TransactionCreateRequestDTO request = new TransactionCreateRequestDTO(5432L, 1, BigDecimal.TEN);

        restTestClient.post()
                .uri(TRANSACTIONS_BASE_PATH)
                .body(request)
                .header("Content-type", "application/json")
                .header("transaction-e2e-id", "8841cd3f-ca5d-448f-ba91-f8391bded498")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message")
                .isEqualTo("Transaction with e2e id 8841cd3f-ca5d-448f-ba91-f8391bded498 already exists")
                .jsonPath("$.path").isEqualTo(TRANSACTIONS_BASE_PATH);

        verify(transactionService).createTransaction(any(Transaction.class));
        verify(transactionRepository).existsByE2eId(any(UUID.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountService, never()).findById(5432L);
    }

    @ParameterizedTest
    @MethodSource("invalidTransactionsWithDifferentOperations")
    void shouldReturnBadRequestWhenSendInvalidRequest(final TransactionCreateRequestDTO request, final String field,
            final String message) {
        restTestClient.post()
                .uri(TRANSACTIONS_BASE_PATH)
                .body(request)
                .header("Content-type", "application/json")
                .header("transaction-e2e-id", "8841cd3f-ca5d-448f-ba91-f8391bded498")
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message")
                .isEqualTo("invalid request")
                .jsonPath("$.path").isEqualTo(TRANSACTIONS_BASE_PATH)
                .jsonPath("$.errors[0].field").isEqualTo(field)
                .jsonPath("$.errors[0].message").isEqualTo(message);

        verify(transactionService, never()).createTransaction(any(Transaction.class));
        verify(transactionRepository, never()).existsByE2eId(any(UUID.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountService, never()).findById(5432L);
    }

    static Stream<Arguments> validTransactionsWithDifferentOperations() {
        return Stream.of(
                Arguments.of(new TransactionCreateRequestDTO(5432L, 1, BigDecimal.TEN), OperationType.NORMAL_PURCHASE,
                        new BigDecimal("-10.00")),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 2, BigDecimal.TEN),
                        OperationType.PURCHASE_WITH_INSTALLMENTS, new BigDecimal("-10.00")),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 3, BigDecimal.TEN), OperationType.WITHDRAWAL,
                        new BigDecimal("-10.00")),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 4, BigDecimal.TEN), OperationType.CREDIT_VOUCHER,
                        new BigDecimal("10.00")));
    }

    static Stream<Arguments> invalidTransactionsWithDifferentOperations() {
        return Stream.of(
                Arguments.of(new TransactionCreateRequestDTO(null, 1, BigDecimal.TEN), "accountId",
                        "Account id is mandatory"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, null, BigDecimal.TEN),
                        "operationTypeId", "Operation type is mandatory"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 0, BigDecimal.TEN),
                        "operationTypeId", "Operation type value must be between 1 and 4"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 5, BigDecimal.TEN),
                        "operationTypeId", "Operation type value must be between 1 and 4"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 3, null), "amount",
                        "Transaction value is mandatory"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 3, BigDecimal.TEN.negate()), "amount",
                        "Transaction value must be positive"),
                Arguments.of(new TransactionCreateRequestDTO(5432L, 3, BigDecimal.ZERO), "amount",
                        "Transaction value must be positive"));
    }

    private List<Transaction> findTransactions() {
        return jdbcClient.sql(
                """
                        select
                            id,
                            account_id,
                            amount,
                            event_date,
                            transaction_e2e_id,
                            operation_type
                        from
                            transactions
                        """)
                .query((rs, rowNum) -> new Transaction(rs.getLong("account_id"),
                        rs.getInt("operation_type"),
                        rs.getBigDecimal("amount"),
                        UUID.fromString(rs.getString("transaction_e2e_id"))))
                .list();
    }

}
