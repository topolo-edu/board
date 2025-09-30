package io.goorm.board.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증되지 않은 사용자 예외")
public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String message) {
        super(message);
    }
}