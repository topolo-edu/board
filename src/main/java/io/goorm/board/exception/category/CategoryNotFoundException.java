package io.goorm.board.exception.category;

/**
 * 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class CategoryNotFoundException extends RuntimeException {

    private final Long categorySeq;

    public CategoryNotFoundException(Long categorySeq) {
        super("카테고리를 찾을 수 없습니다. categorySeq: " + categorySeq);
        this.categorySeq = categorySeq;
    }

    public CategoryNotFoundException(String message) {
        super(message);
        this.categorySeq = null;
    }

    public Long getCategorySeq() {
        return categorySeq;
    }
}