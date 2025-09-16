package io.goorm.board.exception.product;

/**
 * 상품 이미지 처리 중 발생하는 예외
 */
public class ProductImageException extends RuntimeException {

    public ProductImageException(String message) {
        super(message);
    }

    public ProductImageException(String message, Throwable cause) {
        super(message, cause);
    }
}