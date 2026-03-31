package codex.mmxxvi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketItem {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Field("ticket_id")
    private UUID ticketId;

    private String name;

    private String description;

    @Field("stock_initial")
    private Integer stockInitial;

    @Field("stock_available")
    private Integer stockAvailable;

    @Field("is_stock_prepared")
    private boolean stockPrepared;

    @Field("price_original")
    private Long priceOriginal;

    @Field("price_flash")
    private Long priceFlash;

    @Field("sale_start_time")
    private LocalDateTime saleStartTime;

    @Field("sale_end_time")
    private LocalDateTime saleEndTime;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("deleted_at")
    private LocalDateTime deletedAt;
}