package io.goorm.board.exception;

public class InvalidCredentialsException extends RuntimeException {
    private final String email;
    
    public InvalidCredentialsException(String email) {
        super();
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}