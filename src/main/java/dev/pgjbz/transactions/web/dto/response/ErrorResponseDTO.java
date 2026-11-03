package dev.pgjbz.transactions.web.dto.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(NON_NULL)
public record ErrorResponseDTO(
    @Schema(description = "error message", example = "Not found")
    String message,
    @Schema(description = "path of request", example = "/accounts")
    String path,
    @Schema(description = "http status code", example = "400")
    int status,
    @Schema(description = "list of errors", example = "document must be a valid CPF", implementation = ErrorDetail.class)
    Set<ErrorDetail> errors
) {
    
}
