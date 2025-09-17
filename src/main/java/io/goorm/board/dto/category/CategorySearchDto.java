package io.goorm.board.dto.category;

import io.goorm.board.dto.common.BaseSearchConditionDto;
import io.goorm.board.enums.CategoryStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 카테고리 검색용 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategorySearchDto extends BaseSearchConditionDto {

    private CategoryStatus status;

    @Override
    public boolean isEmpty() {
        return !hasKeyword() && status == null;
    }

    /**
     * 검색 조건 존재 여부 확인
     */
    public boolean hasSearchCondition() {
        return hasKeyword() || status != null;
    }

}