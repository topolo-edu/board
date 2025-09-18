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

    /**
     * 상태 필터 여부 확인
     */
    public boolean hasStatus() {
        return status != null;
    }

    @Override
    public boolean isEmpty() {
        return !hasKeyword() && !hasStatus();
    }

    /**
     * 검색 조건 존재 여부 확인
     */
    public boolean hasSearchCondition() {
        return hasKeyword() || hasStatus();
    }

}