package io.goorm.board.dto.supplier;

import io.goorm.board.dto.common.BaseSearchConditionDto;
import io.goorm.board.enums.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 공급업체 검색용 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierSearchDto extends BaseSearchConditionDto {

    private String email;
    private SupplierStatus status;

    /**
     * 이메일 검색 여부 확인
     */
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    /**
     * 상태 필터 여부 확인
     */
    public boolean hasStatus() {
        return status != null;
    }

    @Override
    public boolean isEmpty() {
        return !hasKeyword() && !hasEmail() && !hasStatus();
    }
}