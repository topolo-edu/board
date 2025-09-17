package io.goorm.board.dto.buyer;

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
public class BuyerExcelDto {

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
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}