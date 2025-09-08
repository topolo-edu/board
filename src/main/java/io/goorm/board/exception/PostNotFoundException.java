package io.goorm.board.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends RuntimeException {
    
    private final Long postId;
    
    public PostNotFoundException(Long postId) {
        super();
        this.postId = postId;
    }
    
    public Long getPostId() {
        return postId;
    }
}