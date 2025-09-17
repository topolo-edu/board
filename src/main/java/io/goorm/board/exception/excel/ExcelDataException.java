package io.goorm.board.exception.excel;

/**
 * Excel 데이터 처리 실패 예외
 */
public class ExcelDataException extends ExcelExportException {

    public ExcelDataException(String message, Throwable cause) {
        super("EXCEL_DATA_PROCESSING_FAILED", message, cause);
    }

    public ExcelDataException(String message) {
        super("EXCEL_DATA_PROCESSING_FAILED", message);
    }
}