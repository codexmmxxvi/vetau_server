package codex.mmxxvi.services;

import codex.mmxxvi.dto.request.CreatePaymentRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.RefundRequest;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.PaymentInitResponse;
import codex.mmxxvi.dto.response.PaymentResponse;
import codex.mmxxvi.dto.response.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface PaymentService {
    PaymentInitResponse createPayment(CreatePaymentRequest createPaymentRequest, HttpServletRequest request);
    PageResponse<PaymentResponse> getPayments(PageRequestDto pageRequestDto);
    PaymentResponse getPaymentByTransactionId(UUID transactionId);
    RefundResponse refundPayment(UUID transactionId, RefundRequest refundRequest);
    int handleCallback(HttpServletRequest request);
}
