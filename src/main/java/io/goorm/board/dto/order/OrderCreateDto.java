package io.goorm.board.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 발주 등록 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDto {

    @NotEmpty(message = "발주 상품을 선택해주세요")
    @Valid
    private List<OrderItemCreateDto> items;

    @Size(max = 500, message = "비고는 500자 이내로 입력해주세요")
    private String notes;

    // 시스템에서 설정할 필드들
    private Long companySeq;
    private Long userSeq;
}