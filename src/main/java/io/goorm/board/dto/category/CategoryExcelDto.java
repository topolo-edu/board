package io.goorm.board.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카테고리 Excel 출력용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExcelDto {

    private String name;
    private String description;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
}