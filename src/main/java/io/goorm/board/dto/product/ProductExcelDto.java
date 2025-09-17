package io.goorm.board.dto.product;

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
public class ProductExcelDto {
    private String name;
    private String code;
    private String categoryName;
    private String supplierName;
    private BigDecimal unitPrice;
    private BigDecimal unitCost;
    private String unit;
    private String status;
    private String sku;
    private String barcode;
    private LocalDateTime createdAt;
}