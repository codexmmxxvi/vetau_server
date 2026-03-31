package codex.mmxxvi.controller;


import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.ResponseTicket;
import codex.mmxxvi.entity.Ticket;
import codex.mmxxvi.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RequestMapping("/v1")
public class TicketController {
    @Autowired
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    @GetMapping("/tickets")
    public List<ResponseTicket> getTickets(){
        return ticketService.getTickets();
    }
    @GetMapping("/tickets/{id}")
    public ResponseTicket getTicket(UUID id) {
        return ticketService.getTicket(id);
    }
    @PatchMapping("/tickets/{id}")
    public ResponseTicket updateTicket(UUID id,UpdateTicketRequest updateTicketRequest) {
        return ticketService.updateTicket(id, updateTicketRequest);
    }
    @DeleteMapping("/tickets/{id}")
    public void deleteTicket(UUID id) {
        ticketService.deleteTicket(id);
    }
}
