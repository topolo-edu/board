package io.goorm.board.exception.product;

/**
 * 상품 코드 중복 시 발생하는 예외
 */
public class ProductCodeDuplicateException extends RuntimeException {

    public ProductCodeDuplicateException(String code) {
        super("Product code already exists: " + code);
    }

    public ProductCodeDuplicateException(String code, Throwable cause) {
        super("Product code already exists: " + code, cause);
    }
}