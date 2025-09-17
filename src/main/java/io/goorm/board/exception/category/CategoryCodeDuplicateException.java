package io.goorm.board.exception.category;

/**
 * 카테고리 코드 중복 시 발생하는 예외
 */
public class CategoryCodeDuplicateException extends RuntimeException {

    private final String code;

    public CategoryCodeDuplicateException(String code) {
        super("이미 존재하는 카테고리 코드입니다. code: " + code);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}