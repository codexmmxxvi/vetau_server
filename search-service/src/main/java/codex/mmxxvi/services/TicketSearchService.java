package codex.mmxxvi.services;

import codex.mmxxvi.dto.request.IndexTicketRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.SearchTicketResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TicketSearchService {
    Mono<PageResponse<SearchTicketResponse>> searchTickets(String keyword, PageRequestDto pageRequestDto);
    Mono<Void> indexTicket(IndexTicketRequest request);
    Mono<Void> deleteTicket(UUID ticketId);
}
