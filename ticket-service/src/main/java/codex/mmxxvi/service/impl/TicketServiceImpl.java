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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class TicketServiceImpl implements TicketService {
    private static final int ROLE_ADMIN = 1;

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
    public Mono<PageResponse<ResponseTicket>> getTickets(PageRequestDto pageRequestDto) {
        return resolveAuthContext().flatMap(authContext ->
            Mono.fromCallable(() -> {
                    requireAnyScope(authContext, "ticket.read", "ticket.write");

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
                })
                .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @Override
    public Mono<ResponseTicket> getTicket(UUID ticketId) {
        return resolveAuthContext().flatMap(authContext ->
                Mono.fromCallable(() -> {
                            requireAnyScope(authContext, "ticket.read", "ticket.write");
                            return ticketRepository.findTicketById(ticketId);
                        })
                        .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @Override
    public Mono<ResponseTicket> createTicket() {
        return resolveAuthContext().flatMap(authContext ->
                Mono.fromCallable(() -> {
                            requireAnyScope(authContext, "ticket.write");
                            requireAdmin(authContext);

                            Ticket ticket = new Ticket();
                            ticket.setId(UUID.randomUUID());
                            return toResponse(ticket);
                        })
                        .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @Override
    public Mono<ResponseTicket> updateTicket(UUID ticketId, UpdateTicketRequest updateTicketRequest) {
        return resolveAuthContext().flatMap(authContext ->
                Mono.fromCallable(() -> {
                            requireAnyScope(authContext, "ticket.write");
                            requireAdmin(authContext);

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
                        })
                        .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @Override
    public Mono<ResponseTicket> updateTicketItems(UUID ticketId, UpdateTicketItemsRequest updateTicketItemsRequest) {
        return resolveAuthContext().flatMap(authContext ->
            Mono.fromCallable(() -> {
                    requireAnyScope(authContext, "ticket.write");
                    requireAdmin(authContext);

                    var ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Ticket not found"));

                    ticket.setTicketItems(toTicketItems(ticketId, updateTicketItemsRequest.getTicketItems()));
                    return toResponse(ticketRepository.save(ticket));
                })
                .subscribeOn(Schedulers.boundedElastic())
        );
    }

    @Override
    public Mono<Void> deleteTicket(UUID ticketId) {
        return resolveAuthContext().flatMap(authContext -> {
            requireAnyScope(authContext, "ticket.write");
            requireAdmin(authContext);

            return Mono.fromRunnable(() -> ticketRepository.deleteById(ticketId))
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
        });
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

    private Mono<AuthContext> resolveAuthContext() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .map(Jwt.class::cast)
                .map(this::toAuthContext)
                .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "Unauthorized")));
    }

    private AuthContext toAuthContext(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenantId");
        if (!StringUtils.hasText(tenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Token tenantId claim is missing");
        }

        Object userIdClaim = jwt.getClaims().get("userId");
        if (userIdClaim == null) {
            throw new ResponseStatusException(FORBIDDEN, "Token userId claim is missing");
        }

        UUID userId;
        try {
            userId = UUID.fromString(String.valueOf(userIdClaim));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(FORBIDDEN, "Token userId claim is invalid");
        }

        Object roleClaim = jwt.getClaims().get("role");
        int role;
        if (roleClaim instanceof Number number) {
            role = number.intValue();
        } else {
            try {
                role = Integer.parseInt(String.valueOf(roleClaim));
            } catch (Exception ex) {
                throw new ResponseStatusException(FORBIDDEN, "Token role claim is invalid");
            }
        }

        Set<String> scopes = parseScopes(jwt.getClaims().get("scope"));
        return new AuthContext(userId, role, scopes);
    }

    private Set<String> parseScopes(Object scopeClaim) {
        if (scopeClaim == null) {
            return Collections.emptySet();
        }

        if (scopeClaim instanceof String scopeText) {
            if (!StringUtils.hasText(scopeText)) {
                return Collections.emptySet();
            }
            return Arrays.stream(scopeText.trim().split("\\s+"))
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (scopeClaim instanceof Iterable<?> iterable) {
            Set<String> scopes = new LinkedHashSet<>();
            for (Object value : iterable) {
                if (value != null && StringUtils.hasText(String.valueOf(value))) {
                    scopes.add(String.valueOf(value));
                }
            }
            return scopes;
        }

        return Collections.emptySet();
    }

    private void requireAnyScope(AuthContext authContext, String... expectedScopes) {
        for (String expectedScope : expectedScopes) {
            if (authContext.scopes().contains(expectedScope)) {
                return;
            }
        }
        throw new ResponseStatusException(FORBIDDEN, "Insufficient scope");
    }

    private void requireAdmin(AuthContext authContext) {
        if (!authContext.isAdmin()) {
            throw new ResponseStatusException(FORBIDDEN, "Admin role is required");
        }
    }

    private record AuthContext(UUID userId, int role, Set<String> scopes) {
        boolean isAdmin() {
            return role >= ROLE_ADMIN;
        }
    }
}
