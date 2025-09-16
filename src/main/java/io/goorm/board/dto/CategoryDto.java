package io.goorm.board.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 카테고리 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long categorySeq;
    private String name;
    private String description;
    private Long parentCategorySeq;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdSeq;
    private Long updatedSeq;
}