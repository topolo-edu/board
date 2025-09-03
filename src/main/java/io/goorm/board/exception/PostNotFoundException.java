package io.goorm.board.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends RuntimeException {
    
    public PostNotFoundException(String message) {
        super(message);
    }
    
    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PostNotFoundException(Long seq) {
        super("게시글을 찾을 수 없습니다. seq: " + seq);
    }
}