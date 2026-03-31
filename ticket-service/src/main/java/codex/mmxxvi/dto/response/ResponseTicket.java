package codex.mmxxvi.dto.response;

import codex.mmxxvi.entity.TicketItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTicket {
    private UUID id;
    private String title;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private List<TicketItem> ticketItems;
}
