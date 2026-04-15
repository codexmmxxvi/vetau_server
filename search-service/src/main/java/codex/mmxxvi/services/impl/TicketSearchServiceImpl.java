package codex.mmxxvi.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import codex.mmxxvi.dto.request.IndexTicketRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.SearchTicketResponse;
import codex.mmxxvi.services.TicketSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketSearchServiceImpl implements TicketSearchService {

    private final WebClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    private final String ticketIndex;

    public TicketSearchServiceImpl(
            ObjectMapper objectMapper,
            @Value("${search.elasticsearch.url:${ELASTICSEARCH_URL:http://localhost:9200}}") String elasticsearchUrl,
            @Value("${search.elasticsearch.ticket-index:ticket-search}") String ticketIndex
    ) {
        this.objectMapper = objectMapper;
        this.elasticsearchClient = WebClient.builder()
                .baseUrl(elasticsearchUrl)
                .build();
        this.ticketIndex = ticketIndex;
    }

    @Override
    public Mono<PageResponse<SearchTicketResponse>> searchTickets(String keyword, PageRequestDto pageRequestDto) {
        int pageNo = pageRequestDto.getPageNo();
        int pageSize = pageRequestDto.getPageSize();

        return elasticsearchClient.post()
                .uri("/{index}/_search", ticketIndex)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildSearchBody(keyword, pageNo, pageSize))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .switchIfEmpty(Mono.error(new IllegalStateException("Empty search response from Elasticsearch")))
                .map(responseNode -> toPageResponse(responseNode, pageNo, pageSize))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.just(emptyPage(pageNo, pageSize)))
                .onErrorMap(WebClientResponseException.class, ex -> mapWebClientException("search tickets in Elasticsearch", ex));
    }

    @Override
    public Mono<Void> indexTicket(IndexTicketRequest request) {
        if (request.getId() == null) {
            return Mono.error(new IllegalArgumentException("Ticket id is required"));
        }

        Map<String, Object> payload = objectMapper.convertValue(request, new TypeReference<>() {
        });

        return elasticsearchClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{index}/_doc/{id}")
                        .queryParam("refresh", "wait_for")
                        .build(ticketIndex, request.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .then()
                .onErrorMap(WebClientResponseException.class, ex -> mapWebClientException("index ticket in Elasticsearch", ex));
    }

    @Override
    public Mono<Void> deleteTicket(UUID ticketId) {
        return elasticsearchClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/{index}/_doc/{id}")
                        .queryParam("refresh", "wait_for")
                        .build(ticketIndex, ticketId))
                .retrieve()
                .toBodilessEntity()
                .then()
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                .onErrorMap(WebClientResponseException.class, ex -> mapWebClientException("delete ticket in Elasticsearch", ex));
    }

    private Map<String, Object> buildSearchBody(String keyword, int pageNo, int pageSize) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", pageNo * pageSize);
        payload.put("size", pageSize);
        payload.put("sort", List.of(
                Map.of("updatedAt", Map.of(
                        "order", "desc",
                        "unmapped_type", "date"
                ))
        ));

        if (keyword == null || keyword.isBlank()) {
            payload.put("query", Map.of("match_all", Map.of()));
            return payload;
        }

        String normalizedKeyword = keyword.trim();
        payload.put("query", Map.of(
                "bool", Map.of(
                        "should", List.of(
                                Map.of("match", Map.of("title", Map.of("query", normalizedKeyword, "operator", "and"))),
                                Map.of("match", Map.of("ticketItems.name", Map.of("query", normalizedKeyword, "operator", "and"))),
                                Map.of("match", Map.of("ticketItems.description", Map.of("query", normalizedKeyword, "operator", "and")))
                        ),
                        "minimum_should_match", 1
                )
        ));
        return payload;
    }

    private PageResponse<SearchTicketResponse> toPageResponse(JsonNode responseNode, int pageNo, int pageSize) {
        JsonNode hitsNode = responseNode.path("hits").path("hits");
        JsonNode totalNode = responseNode.path("hits").path("total");

        long totalElements = totalNode.isObject() ? totalNode.path("value").asLong(0L) : totalNode.asLong(0L);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        boolean last = totalPages == 0 || pageNo >= totalPages - 1;

        List<SearchTicketResponse> content = new ArrayList<>();
        for (JsonNode hitNode : hitsNode) {
            JsonNode sourceNode = hitNode.path("_source");
            SearchTicketResponse searchTicket = objectMapper.convertValue(sourceNode, SearchTicketResponse.class);
            if (hitNode.has("_score") && !hitNode.path("_score").isNull()) {
                searchTicket.setScore(hitNode.path("_score").asDouble());
            }
            content.add(searchTicket);
        }

        return PageResponse.<SearchTicketResponse>builder()
                .content(content)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();
    }

    private PageResponse<SearchTicketResponse> emptyPage(int pageNo, int pageSize) {
        return PageResponse.<SearchTicketResponse>builder()
                .content(List.of())
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .build();
    }

    private IllegalStateException mapWebClientException(String action, WebClientResponseException ex) {
        return new IllegalStateException("Failed to " + action + ": " + ex.getResponseBodyAsString(), ex);
    }
}
