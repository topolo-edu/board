package io.goorm.board.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 상품 단위 Enum
 */
@Getter
@RequiredArgsConstructor
public enum ProductUnit {
    EA("EA", "개"),
    SET("SET", "세트"),
    KG("KG", "킬로그램"),
    G("G", "그램"),
    L("L", "리터"),
    ML("ML", "밀리리터"),
    M("M", "미터"),
    CM("CM", "센티미터"),
    BOX("BOX", "박스"),
    PACK("PACK", "팩"),
    BOTTLE("BOTTLE", "병"),
    CAN("CAN", "캔");

    private final String code;
    private final String displayName;

    /**
     * 코드로 ProductUnit 찾기
     */
    public static ProductUnit fromCode(String code) {
        for (ProductUnit unit : values()) {
            if (unit.code.equals(code)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown ProductUnit code: " + code);
    }

    /**
     * 무게 단위 여부 확인
     */
    public boolean isWeightUnit() {
        return this == KG || this == G;
    }

    /**
     * 부피 단위 여부 확인
     */
    public boolean isVolumeUnit() {
        return this == L || this == ML;
    }

    /**
     * 길이 단위 여부 확인
     */
    public boolean isLengthUnit() {
        return this == M || this == CM;
    }
}