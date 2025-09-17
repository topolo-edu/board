package io.goorm.board.dto.buyer;

import io.goorm.board.enums.BuyerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerSearchDto {

    private String keyword;
    private String email;
    private String businessNumber;
    private BuyerStatus status;

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;
}