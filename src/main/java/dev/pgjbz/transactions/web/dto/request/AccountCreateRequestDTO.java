package dev.pgjbz.transactions.web.dto.request;

import org.hibernate.validator.constraints.br.CPF;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AccountCreateRequestDTO(
    @NotBlank(message = "{document.empty}")
    @CPF(message = "{document.invalid}") 
    @Schema(description = "account holder document to create account", example = "00000000121")
    String documentNumber) {

}