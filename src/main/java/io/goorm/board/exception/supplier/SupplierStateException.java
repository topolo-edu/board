package io.goorm.board.exception.supplier;

import io.goorm.board.enums.SupplierStatus;

/**
 * 공급업체 상태 변경 실패시 발생하는 예외
 */
public class SupplierStateException extends RuntimeException {

    private final Long supplierSeq;
    private final SupplierStatus currentStatus;
    private final SupplierStatus targetStatus;

    public SupplierStateException(Long supplierSeq, SupplierStatus currentStatus, SupplierStatus targetStatus, String message) {
        super(message);
        this.supplierSeq = supplierSeq;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public SupplierStateException(Long supplierSeq, SupplierStatus currentStatus, SupplierStatus targetStatus) {
        super();
        this.supplierSeq = supplierSeq;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public SupplierStateException(String message) {
        super(message);
        this.supplierSeq = null;
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public SupplierStateException(String message, Throwable cause) {
        super(message, cause);
        this.supplierSeq = null;
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public Long getSupplierSeq() {
        return supplierSeq;
    }

    public SupplierStatus getCurrentStatus() {
        return currentStatus;
    }

    public SupplierStatus getTargetStatus() {
        return targetStatus;
    }
}