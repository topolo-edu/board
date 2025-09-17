package io.goorm.board.exception.excel;

/**
 * Excel 내보내기 관련 예외
 */
public class ExcelExportException extends RuntimeException {

    private final String errorCode;

    public ExcelExportException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExcelExportException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}