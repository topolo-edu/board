package io.goorm.board.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카테고리 등록용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto {

    // 공용 폼 사용을 위해 추가 (등록시에는 null, validation 제외)
    private Long categorySeq;

    @NotBlank(message = "{category.validation.name.notblank}")
    @Size(max = 100, message = "{category.validation.name.size}")
    private String name;

    @Size(max = 500, message = "{category.validation.description.size}")
    private String description;

    @Builder.Default
    private Integer sortOrder = 0;
}