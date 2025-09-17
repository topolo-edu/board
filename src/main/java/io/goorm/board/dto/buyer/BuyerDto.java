package io.goorm.board.dto.buyer;

import io.goorm.board.enums.BuyerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDto {

    private Long buyerSeq;
    private String companyName;
    private String businessNumber;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private BigDecimal creditLimit;
    private BigDecimal discountRate;
    private BigDecimal totalOrderAmount;
    private LocalDateTime lastOrderDate;
    private String paymentTerms;
    private String description;
    private BuyerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;
}