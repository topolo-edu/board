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
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;

    /**
     * Boolean isActive를 CategoryStatus로 변환
     */
    public CategoryStatus getStatus() {
        return Boolean.TRUE.equals(isActive) ? CategoryStatus.ACTIVE : CategoryStatus.INACTIVE;
    }

    /**
     * 상태 표시명 반환
     */
    public String getStatusDisplayName() {
        return getStatus().getDisplayName();
    }
}