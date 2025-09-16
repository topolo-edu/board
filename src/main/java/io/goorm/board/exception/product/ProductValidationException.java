package io.goorm.board.exception.product;

/**
 * 상품 데이터 검증 실패 시 발생하는 예외
 */
public class ProductValidationException extends RuntimeException {

    public ProductValidationException(String message) {
        super(message);
    }

    public ProductValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}