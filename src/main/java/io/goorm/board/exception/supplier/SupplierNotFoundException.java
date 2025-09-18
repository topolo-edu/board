package io.goorm.board.exception.supplier;

/**
 * 공급업체를 찾을 수 없을 때 발생하는 예외
 */
public class SupplierNotFoundException extends RuntimeException {

    private final Long supplierSeq;

    public SupplierNotFoundException(Long supplierSeq) {
        super("공급업체를 찾을 수 없습니다. supplierSeq: " + supplierSeq);
        this.supplierSeq = supplierSeq;
    }

    public SupplierNotFoundException(String message) {
        super(message);
        this.supplierSeq = null;
    }

    public Long getSupplierSeq() {
        return supplierSeq;
    }
}