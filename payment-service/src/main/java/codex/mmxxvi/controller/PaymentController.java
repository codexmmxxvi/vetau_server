package codex.mmxxvi.controller;

import codex.mmxxvi.dto.request.CreatePaymentRequest;
import codex.mmxxvi.dto.request.PageRequestDto;
import codex.mmxxvi.dto.request.RefundRequest;
import codex.mmxxvi.dto.response.PageResponse;
import codex.mmxxvi.dto.response.PaymentInitResponse;
import codex.mmxxvi.dto.response.PaymentResponse;
import codex.mmxxvi.dto.response.RefundResponse;
import codex.mmxxvi.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequestMapping("/v1")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${app.client.payment-result-url:http://localhost:3000/payment-result}")
    private String paymentResultUrl;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payments")
    public PageResponse<PaymentResponse> getPayments(@Valid @ModelAttribute PageRequestDto pageRequestDto) {
        return paymentService.getPayments(pageRequestDto);
    }

    @PostMapping({"/payments", "/create"})
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentInitResponse createPayment(
            @Valid @RequestBody CreatePaymentRequest createPaymentRequest,
            HttpServletRequest request
    ) {
        return paymentService.createPayment(createPaymentRequest, request);
    }

    @GetMapping({"/payments/{transactionId}", "/{transactionId}"})
    public PaymentResponse getPaymentByTransactionId(@PathVariable UUID transactionId) {
        return paymentService.getPaymentByTransactionId(transactionId);
    }

    @PostMapping({"/payments/{transactionId}/refund", "/refund"})
    public RefundResponse refundPayment(
            @PathVariable(name = "transactionId", required = false) UUID pathTransactionId,
            @RequestParam(name = "transactionId", required = false) UUID requestTransactionId,
            @Valid @RequestBody RefundRequest refundRequest
    ) {
        UUID transactionId = pathTransactionId != null ? pathTransactionId : requestTransactionId;
        if (transactionId == null) {
            throw new RuntimeException("Transaction id is required");
        }
        return paymentService.refundPayment(transactionId, refundRequest);
    }

    @GetMapping({"/payments/vnpay/callback", "/vnpay-payment"})
    public ResponseEntity<Void> handleVnPayCallback(HttpServletRequest request) {
        int paymentStatus = paymentService.handleCallback(request);
        URI redirectUri = UriComponentsBuilder.fromUriString(paymentResultUrl)
                .queryParam("status", mapClientStatus(paymentStatus))
                .queryParam("orderId", request.getParameter("vnp_OrderInfo"))
                .queryParam("totalPrice", request.getParameter("vnp_Amount"))
                .queryParam("paymentTime", request.getParameter("vnp_PayDate"))
                .queryParam("transactionId", request.getParameter("vnp_TxnRef"))
                .queryParam("gatewayTransactionNo", request.getParameter("vnp_TransactionNo"))
                .queryParam("responseCode", request.getParameter("vnp_ResponseCode"))
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }

    private String mapClientStatus(int paymentStatus) {
        return switch (paymentStatus) {
            case 1 -> "success";
            case 0 -> "failed";
            default -> "invalid";
        };
    }
}
