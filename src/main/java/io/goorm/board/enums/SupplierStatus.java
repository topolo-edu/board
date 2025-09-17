package io.goorm.board.enums;

import lombok.Getter;

@Getter
public enum SupplierStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String displayName;

    SupplierStatus(String displayName) {
        this.displayName = displayName;
    }
}