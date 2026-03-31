package codex.mmxxvi.services;

import codex.mmxxvi.dto.request.CreateOrderRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.response.OrderResponse;
import codex.mmxxvi.dto.response.PageResponse;

import java.util.UUID;


public interface OrderService {
    PageResponse<OrderResponse> getAllOrders(PageRequestDto pageRequestDto);
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse updateStatus(UUID id, Boolean status);
    PageResponse<OrderResponse> filterOrderFollowingStatus(Integer status, PageRequestDto pageRequestDto);
}
