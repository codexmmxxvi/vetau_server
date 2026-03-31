package codex.mmxxvi.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketItemsRequest {
    @Valid
    private List<UpdateTicketItemRequest> ticketItems;
}
