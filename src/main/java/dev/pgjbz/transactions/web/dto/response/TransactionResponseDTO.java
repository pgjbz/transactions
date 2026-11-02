package dev.pgjbz.transactions.web.dto.response;

import java.time.LocalDateTime;

public record TransactionResponseDTO(Long id, LocalDateTime createdAt) {
    
}
