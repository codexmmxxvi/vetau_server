package codex.mmxxvi.service.impl;

import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.ResponseTicket;
import codex.mmxxvi.service.TicketService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    @Override
    public List<ResponseTicket> ListTickets() {
        return List.of();
    }

    @Override
    public ResponseTicket getTicket(UUID ticketId) {
        return null;
    }

    @Override
    public ResponseTicket createTicket() {
        return null;
    }

    @Override
    public ResponseTicket updateTicket(UUID ticketId, UpdateTicketRequest updateTicketRequest) {
        return null;
    }

    @Override
    public ResponseTicket deleteTicket(UUID ticketId) {
        return null;
    }
}
