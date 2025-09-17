package io.goorm.board.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 카테고리 상태 Enum
 */
@Getter
@RequiredArgsConstructor
public enum CategoryStatus {
    ACTIVE("ACTIVE", "사용중"),
    INACTIVE("INACTIVE", "중지");

    private final String code;
    private final String displayName;

    /**
     * 코드로 CategoryStatus 찾기
     */
    public static CategoryStatus fromCode(String code) {
        for (CategoryStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CategoryStatus code: " + code);
    }
}