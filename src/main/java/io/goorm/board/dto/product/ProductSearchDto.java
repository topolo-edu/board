package io.goorm.board.dto.product;

import io.goorm.board.dto.common.BaseSearchConditionDto;
import io.goorm.board.enums.ProductStatus;
import io.goorm.board.enums.ProductUnit;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

/**
 * 상품 검색 조건 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductSearchDto extends BaseSearchConditionDto {

    private String name;
    private String code;
    private Long categorySeq;
    private ProductStatus status;
    private ProductUnit unit;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long supplierSeq;
    private Boolean hasImage;

    @Override
    public boolean isEmpty() {
        return !hasKeyword() &&
               name == null &&
               code == null &&
               categorySeq == null &&
               status == null &&
               unit == null &&
               minPrice == null &&
               maxPrice == null &&
               supplierSeq == null &&
               hasImage == null;
    }

    /**
     * 이름 검색 여부
     */
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * 코드 검색 여부
     */
    public boolean hasCode() {
        return code != null && !code.trim().isEmpty();
    }

    /**
     * 카테고리 검색 여부
     */
    public boolean hasCategorySeq() {
        return categorySeq != null && categorySeq > 0;
    }

    /**
     * 가격 범위 검색 여부
     */
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    /**
     * 공급업체 검색 여부
     */
    public boolean hasSupplierSeq() {
        return supplierSeq != null && supplierSeq > 0;
    }
}