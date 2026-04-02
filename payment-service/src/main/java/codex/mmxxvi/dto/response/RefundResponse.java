package codex.mmxxvi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private UUID paymentId;
    private UUID orderId;
    private Long refundAmount;
    private String paymentMethod;
    private String transactionId;
    private Integer status;
    private LocalDateTime refundedAt;
}
