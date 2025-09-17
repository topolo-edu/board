package io.goorm.board.enums;

public enum BuyerStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String displayName;

    BuyerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}