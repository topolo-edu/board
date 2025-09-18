package io.goorm.board.exception.supplier;

/**
 * 공급업체 중복시 발생하는 예외
 */
public class SupplierDuplicateException extends RuntimeException {

    private final String fieldName;
    private final Object fieldValue;

    public SupplierDuplicateException(String fieldName, Object fieldValue) {
        super("Duplicate supplier " + fieldName + ": " + fieldValue);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public SupplierDuplicateException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public SupplierDuplicateException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}