package codex.mmxxvi.controller;


import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.response.PageResponse;
import jakarta.validation.Valid;
import codex.mmxxvi.dto.request.UpdateTicketItemsRequest;
import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.ResponseTicket;
import codex.mmxxvi.service.TicketService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/v1")
@RestController
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    @GetMapping("/tickets")
    public PageResponse<ResponseTicket> getTickets(PageRequestDto pageRequestDto){
        return ticketService.getTickets(pageRequestDto);
    }
    @GetMapping("/tickets/{id}")
    public ResponseTicket getTicket(@PathVariable UUID id) {
        return ticketService.getTicket(id);
    }
    @PatchMapping("/tickets/{id}")
    public ResponseTicket updateTicket(@PathVariable UUID id, @Valid @RequestBody UpdateTicketRequest updateTicketRequest) {
        return ticketService.updateTicket(id, updateTicketRequest);
    }
    @PutMapping("/tickets/{id}/items")
    public ResponseTicket updateTicketItems(@PathVariable UUID id, @Valid @RequestBody UpdateTicketItemsRequest updateTicketItemsRequest) {
        return ticketService.updateTicketItems(id, updateTicketItemsRequest);
    }
    @DeleteMapping("/tickets/{id}")
    public void deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
    }
}
