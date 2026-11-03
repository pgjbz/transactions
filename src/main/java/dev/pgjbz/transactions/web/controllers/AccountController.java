package dev.pgjbz.transactions.web.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import dev.pgjbz.transactions.domain.account.models.Account;
import dev.pgjbz.transactions.domain.account.services.AccountService;
import dev.pgjbz.transactions.web.dto.request.AccountCreateRequestDTO;
import dev.pgjbz.transactions.web.dto.response.AccountResponseDTO;
import dev.pgjbz.transactions.web.dto.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/accounts")
@Tag(name = "accounts", description = "Account Operations")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Find account with given id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the account", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content) })
    @GetMapping(value = "/{accountId}")
    public AccountResponseDTO findAccountById(@PathVariable final Long accountId) {
        final Account account = accountService.findById(accountId);
        return new AccountResponseDTO(account.getId(), account.getDocument());
    }

    @Operation(summary = "Create an account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account Created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AccountResponseDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid payload", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Account with given document already exists", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))) })
    @PostMapping
    public ResponseEntity<AccountResponseDTO> postMethodName(
            @Valid @RequestBody final AccountCreateRequestDTO accountCreate) {
        final Account account = accountService.createAccount(Account.createForDocument(accountCreate.documentNumber()));
        final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(account.getId())
                .toUri();
        return ResponseEntity.created(location).body(new AccountResponseDTO(account.getId(), account.getDocument()));
    }

}
