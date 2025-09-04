package io.goorm.board.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends RuntimeException {
    
    private final Long postId;
    
    public PostNotFoundException(Long postId) {
        super("Post not found with id: " + postId);
        this.postId = postId;
    }
    
    public PostNotFoundException(String message) {
        super(message);
        this.postId = null;
    }
    
    public Long getPostId() {
        return postId;
    }
}