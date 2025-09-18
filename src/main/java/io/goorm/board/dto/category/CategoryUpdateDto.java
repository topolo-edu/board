package io.goorm.board.dto.category;

import io.goorm.board.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카테고리 수정용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateDto {

    @NotNull(message = "{category.validation.seq.notnull}")
    private Long categorySeq;

    @NotBlank(message = "{category.validation.name.notblank}")
    @Size(max = 100, message = "{category.validation.name.size}")
    private String name;

    @Size(max = 500, message = "{category.validation.description.size}")
    private String description;

    private Integer sortOrder;

    private CategoryStatus status;

    // 사용자 정보 (Controller에서 설정)
    private Long updatedSeq;
}