package io.goorm.board.exception.category;

/**
 * 카테고리 유효성 검사 실패 시 발생하는 예외
 */
public class CategoryValidationException extends RuntimeException {

    public CategoryValidationException(String message) {
        super(message);
    }

    public CategoryValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}