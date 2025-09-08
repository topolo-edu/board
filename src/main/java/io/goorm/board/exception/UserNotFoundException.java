package io.goorm.board.exception;

public class UserNotFoundException extends RuntimeException {
    private final Long userId;
    
    public UserNotFoundException(Long userId) {
        super();
        this.userId = userId;
    }
    
    public UserNotFoundException(String email) {
        super();
        this.userId = null;
    }
    
    public Long getUserId() {
        return userId;
    }
}