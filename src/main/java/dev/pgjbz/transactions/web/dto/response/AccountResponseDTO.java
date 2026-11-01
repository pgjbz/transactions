package dev.pgjbz.transactions.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccountResponseDTO(
    @Schema(description = "id of account", example = "1")
    Long id,
    @Schema(description = "document of account holder", example = "00000000121")
    String documentNumber
) {
    
}
