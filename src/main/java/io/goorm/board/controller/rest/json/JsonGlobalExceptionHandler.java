package io.goorm.board.controller.rest.json;

import io.goorm.board.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON REST API용 글로벌 예외 처리
 * rest.json 패키지에만 적용
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackages = "io.goorm.board.controller.rest.json")
public class JsonGlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * 게시글을 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePostNotFound(PostNotFoundException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.not.found", new Object[]{ex.getPostId()}, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                "POST_NOT_FOUND",
                message
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 엔티티를 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.general", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                message
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 검증 실패 - @Valid 어노테이션 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.validation.failed", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                message
        );
        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 바인딩 실패 (400)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.validation.failed", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "BINDING_FAILED",
                message
        );
        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 잘못된 인자 (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 접근 권한 없음 (403)
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.access.denied", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                message
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 인증되지 않음 (401)
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            org.springframework.security.core.AuthenticationException ex) {

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.authentication.required", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                message
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 기타 런타임 예외 (500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("예상하지 못한 런타임 예외 발생", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.server.internal", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                message
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 모든 예외 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("예상하지 못한 예외 발생", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("error.server.internal", null, locale);

        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "UNEXPECTED_ERROR",
                message
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 공통 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);

        return errorResponse;
    }
}