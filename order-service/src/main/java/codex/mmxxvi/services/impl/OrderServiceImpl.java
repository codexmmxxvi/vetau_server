package codex.mmxxvi.services.impl;

import codex.mmxxvi.dto.request.CreateOrderRequest;
import codex.mmxxvi.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.response.OrderResponse;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.entity.Order;
import codex.mmxxvi.services.OrderService;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private OrderResponse convertDTO(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .ticketItemId(order.getTicketItemId())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }


    @Override
    public PageResponse<OrderResponse> getAllOrders(PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.getPageable();
        Page<Order> orderPage = orderRepository.findAll(pageable);

        return buildPageResponse(orderPage);
    }

    private PageResponse<OrderResponse> buildPageResponse(Page<Order> orderPage) {
        return PageResponse.<OrderResponse>builder()
                .content(orderPage.getContent().stream()
                        .map(this::convertDTO)
                        .toList())
                .pageNo(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPage(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .ticketItemId(request.getTicketItemId())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(request.getTotalPrice())
                .status(request.getStatus() == null ? 0 : request.getStatus())
                .build();

        return convertDTO(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateStatus(UUID id, Boolean status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        order.setStatus(Boolean.TRUE.equals(status) ? 1 : 0);
        return convertDTO(orderRepository.save(order));
    }

    @Override
    public PageResponse<OrderResponse> filterOrderFollowingStatus(Integer status, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.getPageable();
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);
        return buildPageResponse(orderPage);
    }
}
