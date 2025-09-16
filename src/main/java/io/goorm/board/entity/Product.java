package io.goorm.board.entity;

import io.goorm.board.enums.ProductStatus;
import io.goorm.board.enums.ProductUnit;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 엔티티
 */
@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_seq")
    private Long productSeq;

    @NotBlank(message = "{validation.product.code.required}")
    @Size(max = 50, message = "{validation.product.code.size}")
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @NotBlank(message = "{validation.product.name.required}")
    @Size(max = 200, message = "{validation.product.name.size}")
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 4000, message = "{validation.product.description.size}")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_seq")
    private Long categorySeq;

    @Column(name = "supplier_seq")
    private Long supplierSeq;

    @NotNull(message = "{validation.product.price.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.price.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.product.price.format}")
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "{validation.product.cost.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.cost.min}")
    @Digits(integer = 8, fraction = 2, message = "{validation.product.cost.format}")
    @Column(name = "unit_cost", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", length = 20)
    @Builder.Default
    private ProductUnit unit = ProductUnit.EA;

    @Size(max = 100, message = "{validation.product.sku.size}")
    @Column(name = "sku", length = 100)
    private String sku;

    @Size(max = 100, message = "{validation.product.barcode.size}")
    @Column(name = "barcode", length = 100)
    private String barcode;

    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.product.weight.min}")
    @Digits(integer = 5, fraction = 3, message = "{validation.product.weight.format}")
    @Column(name = "weight", precision = 8, scale = 3)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;

    @Size(max = 100, message = "{validation.product.dimensions.size}")
    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Size(max = 500, message = "{validation.product.imageUrl.size}")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotNull(message = "{validation.product.status.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED')", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_seq")
    private Long createdSeq;

    @Column(name = "updated_seq")
    private Long updatedSeq;

    // === 비즈니스 메서드 ===

    /**
     * 마진액 계산
     */
    public BigDecimal getMarginAmount() {
        if (unitPrice == null || unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.subtract(unitCost);
    }

    /**
     * 마진율 계산 (백분율)
     */
    public BigDecimal getMarginRate() {
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getMarginAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(unitCost, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 판매 가능 여부
     */
    public boolean isSellable() {
        return status != null && status.isSellable();
    }

    /**
     * 활성 상태 여부
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * 이미지 존재 여부
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }

    /**
     * 가격 검증 - 판매가가 원가보다 높아야 함
     */
    public boolean isPriceValid() {
        if (unitPrice == null || unitCost == null) {
            return false;
        }
        return unitPrice.compareTo(unitCost) >= 0;
    }
}