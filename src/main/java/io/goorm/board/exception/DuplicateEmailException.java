package io.goorm.board.exception;

public class DuplicateEmailException extends RuntimeException {
    private final String email;
    
    public DuplicateEmailException(String email) {
        super();
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}