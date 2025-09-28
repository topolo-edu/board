package io.goorm.board.controller.rest.responseentity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "io.goorm.board.controller.rest.responseentity")
public class ResponseEntityGlobalExceptionHandler {

    // 404 - 리소스를 찾을 수 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        log.error("EntityNotFoundException: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // 403 - 접근 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.error("AccessDeniedException: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            "해당 리소스에 접근할 권한이 없습니다."
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // 400 - 입력값 검증 오류
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_FAILED",
            "입력값 검증에 실패했습니다."
        );
        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 500 - 일반적인 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // 공통 성공 응답 생성
    public static Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    // 공통 성공 응답 생성 (추가 데이터 포함)
    public static Map<String, Object> createSuccessResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = createSuccessResponse(message);
        response.putAll(data);
        return response;
    }

    // 공통 에러 응답 생성
    private Map<String, Object> createErrorResponse(HttpStatus status, String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", Map.of(
            "code", code,
            "message", message,
            "status", status.value(),
            "timestamp", LocalDateTime.now()
        ));
        return response;
    }
}