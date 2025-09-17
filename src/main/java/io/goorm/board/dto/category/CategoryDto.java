package io.goorm.board.dto.category;

import io.goorm.board.enums.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카테고리 정보 전송용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long categorySeq;
    private String name;
    private String description;
    private Integer sortOrder;
    private CategoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;

    /**
     * 카테고리 활성 상태 확인
     */
    public boolean isActive() {
        return CategoryStatus.ACTIVE.equals(this.status);
    }

    /**
     * 상태 표시명 반환
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
}