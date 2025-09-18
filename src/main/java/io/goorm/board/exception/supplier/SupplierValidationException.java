package io.goorm.board.exception.supplier;

/**
 * 공급업체 유효성 검증 실패시 발생하는 예외
 */
public class SupplierValidationException extends RuntimeException {

    private final String field;
    private final Object value;

    public SupplierValidationException() {
        super();
        this.field = null;
        this.value = null;
    }

    public SupplierValidationException(String message) {
        super(message);
        this.field = null;
        this.value = null;
    }

    public SupplierValidationException(String field, Object value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public SupplierValidationException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.value = null;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}