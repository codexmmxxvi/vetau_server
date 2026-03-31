package codex.mmxxvi.service;

import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.ResponseTicket;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    List<ResponseTicket> ListTickets();
    ResponseTicket getTicket(UUID ticketId);
    ResponseTicket createTicket();
    ResponseTicket updateTicket(UUID ticketId, UpdateTicketRequest updateTicketRequest);
    ResponseTicket deleteTicket(UUID ticketId);
}
