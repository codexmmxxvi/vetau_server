package codex.mmxxvi.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketItemRequest {
    private UUID id;
    private String name;
    private String description;

    @PositiveOrZero
    private Integer stockInitial;

    @PositiveOrZero
    private Integer stockAvailable;

    private Boolean stockPrepared;

    @PositiveOrZero
    private Long priceOriginal;

    @PositiveOrZero
    private Long priceFlash;

    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
}
