package codex.mmxxvi.service;

import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.UpdateTicketItemsRequest;
import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.ResponseTicket;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    PageResponse<ResponseTicket> getTickets(PageRequestDto pageRequestDto);
    ResponseTicket getTicket(UUID ticketId);
    ResponseTicket createTicket();
    ResponseTicket updateTicket(UUID ticketId, UpdateTicketRequest updateTicketRequest);
    ResponseTicket updateTicketItems(UUID ticketId, UpdateTicketItemsRequest updateTicketItemsRequest);
    void deleteTicket(UUID ticketId);
}
