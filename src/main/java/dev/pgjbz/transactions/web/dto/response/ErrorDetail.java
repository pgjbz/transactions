package dev.pgjbz.transactions.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorDetail(
    @Schema(description = "field with error", example = "document_number")
    String field, 
    @Schema(description = "Document must be a valid CPF")
    String message) {
    
}
