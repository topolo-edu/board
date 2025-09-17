package io.goorm.board.exception.excel;

/**
 * Excel 파일명 생성 실패 예외
 */
public class ExcelFileNameException extends ExcelExportException {

    public ExcelFileNameException(String message, Throwable cause) {
        super("EXCEL_FILENAME_GENERATION_FAILED", message, cause);
    }

    public ExcelFileNameException(String message) {
        super("EXCEL_FILENAME_GENERATION_FAILED", message);
    }
}