package io.goorm.board.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 상태 Enum
 */
@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("ACTIVE", "판매중"),
    INACTIVE("INACTIVE", "비활성"),
    DISCONTINUED("DISCONTINUED", "단종");

    private final String code;
    private final String displayName;

    /**
     * 코드로 ProductStatus 찾기
     */
    public static ProductStatus fromCode(String code) {
        for (ProductStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus code: " + code);
    }

    /**
     * 활성 상태 여부 확인
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 판매 가능 상태 여부 확인
     */
    public boolean isSellable() {
        return this == ACTIVE;
    }
}