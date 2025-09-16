package io.goorm.board.exception.product;

/**
 * 상품을 찾을 수 없을 때 발생하는 예외
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productSeq) {
        super("Product not found with seq: " + productSeq);
    }

    public ProductNotFoundException(String code) {
        super("Product not found with code: " + code);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}