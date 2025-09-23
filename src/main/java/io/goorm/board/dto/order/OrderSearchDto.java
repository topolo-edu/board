package io.goorm.board.dto.order;

import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 발주 검색 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchDto {

    private String orderNumber;
    private Long companySeq;
    private OrderStatus status;
    private DeliveryStatus deliveryStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // 페이징 정보
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sort = "orderDate";

    @Builder.Default
    private String direction = "desc";
}