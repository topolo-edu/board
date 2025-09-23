package io.goorm.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 발주 상품 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long orderItemSeq;
    private Long orderSeq;
    private Long productSeq;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
    private LocalDateTime createdAt;

    // 조인 필드
    private String productName;
    private String productCode;
    private String categoryName;

    /**
     * 라인 총액 계산 (할인 적용 후)
     */
    public void calculateLineTotal() {
        BigDecimal originalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.lineTotal = originalAmount.subtract(discountAmount);
    }
}