package io.goorm.board.dto.supplier;

import io.goorm.board.enums.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공급업체 검색용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSearchDto {

    private String keyword;

    private String email;

    private SupplierStatus status;

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    public int getOffset() {
        return (page - 1) * size;
    }
}