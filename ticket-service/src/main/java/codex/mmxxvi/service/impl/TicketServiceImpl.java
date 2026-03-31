package codex.mmxxvi.service.impl;

import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.UpdateTicketItemRequest;
import codex.mmxxvi.dto.request.UpdateTicketItemsRequest;
import codex.mmxxvi.dto.request.UpdateTicketRequest;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.ResponseTicket;
import codex.mmxxvi.entity.Ticket;
import codex.mmxxvi.entity.TicketItem;
import codex.mmxxvi.exception.AppExceptions;
import codex.mmxxvi.repository.TicketRepository;
import codex.mmxxvi.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    private ResponseTicket toResponse(Ticket ticket) {
        return ResponseTicket.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .dateStart(ticket.getDateStart())
                .dateEnd(ticket.getDateEnd())
                .ticketItems(ticket.getTicketItems())
                .build();
    }

    @Override
    public PageResponse<ResponseTicket> getTickets(PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.getPageable();
        Page<Ticket> ticketPage = ticketRepository.findAll(pageable);
        return PageResponse.<ResponseTicket>builder()
                .content(ticketPage.getContent().stream().map(this::toResponse).toList())
                .pageNo(ticketPage.getNumber())
                .pageSize(ticketPage.getSize())
                .totalElements(ticketPage.getTotalElements())
                .totalPages(ticketPage.getTotalPages())
                .last(ticketPage.isLast())
                .build();
    }

    @Override
    public ResponseTicket getTicket(UUID ticketId) {
        return ticketRepository.findTicketById(ticketId);
    }

    @Override
    public ResponseTicket createTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        return toResponse(ticket);
    }

    @Override
    public ResponseTicket updateTicket(UUID ticketId, UpdateTicketRequest updateTicketRequest) {
        var oldTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Ticket not found"));

        if (updateTicketRequest.getTitle() != null) {
            oldTicket.setTitle(updateTicketRequest.getTitle().trim());
        }

        if (updateTicketRequest.getDateStart() != null) {
            oldTicket.setDateStart(updateTicketRequest.getDateStart());
        }
        if (updateTicketRequest.getDateEnd() != null) {
            oldTicket.setDateEnd(updateTicketRequest.getDateEnd());
        }

        var updatedTicket = ticketRepository.save(oldTicket);
        return toResponse(updatedTicket);
    }

    @Override
    public ResponseTicket updateTicketItems(UUID ticketId, UpdateTicketItemsRequest updateTicketItemsRequest) {
        var ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Ticket not found"));

        ticket.setTicketItems(toTicketItems(ticketId, updateTicketItemsRequest.getTicketItems()));
        return toResponse(ticketRepository.save(ticket));
    }

    @Override
    public void deleteTicket(UUID ticketId) {
        ticketRepository.deleteById(ticketId);
    }

    private List<TicketItem> toTicketItems(UUID ticketId, List<UpdateTicketItemRequest> requestItems) {
        if (requestItems == null) {
            return List.of();
        }

        List<TicketItem> ticketItems = new ArrayList<>(requestItems.size());
        for (UpdateTicketItemRequest requestItem : requestItems) {
            if (requestItem == null) {
                continue;
            }

            String name = requestItem.getName() == null ? null : requestItem.getName().trim();
            String description = requestItem.getDescription() == null ? null : requestItem.getDescription().trim();

            ticketItems.add(TicketItem.builder()
                    .id(requestItem.getId() != null ? requestItem.getId() : UUID.randomUUID())
                    .ticketId(ticketId)
                    .name(name)
                    .description(description)
                    .stockInitial(requestItem.getStockInitial())
                    .stockAvailable(requestItem.getStockAvailable())
                    .stockPrepared(Boolean.TRUE.equals(requestItem.getStockPrepared()))
                    .priceOriginal(requestItem.getPriceOriginal())
                    .priceFlash(requestItem.getPriceFlash())
                    .saleStartTime(requestItem.getSaleStartTime())
                    .saleEndTime(requestItem.getSaleEndTime())
                    .build());
        }

        return ticketItems;
    }
}
