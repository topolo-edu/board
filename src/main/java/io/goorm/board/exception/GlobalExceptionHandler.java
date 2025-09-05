package io.goorm.board.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 전역 예외 처리기
 * 모든 컨트롤러에서 발생하는 예외를 한 곳에서 처리
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Autowired
    private MessageSource messageSource;

    /**
     * PostNotFoundException 처리
     * 게시글을 찾을 수 없을 때 404 페이지로 이동
     */
    @ExceptionHandler(PostNotFoundException.class)
    public String handlePostNotFoundException(PostNotFoundException e, Model model) {
        logger.warn("Post not found: {}", e.getMessage());

        // 국제화 메시지 조회
        String errorMessage = messageSource.getMessage(
            "error.post.notfound",
            null,
            LocaleContextHolder.getLocale()
        );

        model.addAttribute("error", errorMessage);
        model.addAttribute("postId", e.getPostId());

        return "error/404";
    }

    /**
     * DuplicateEmailException 처리
     * 이메일 중복 시 회원가입 폼으로 리다이렉트
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public String handleDuplicateEmailException(DuplicateEmailException e, Model model) {
        logger.warn("Duplicate email: {}", e.getMessage());

        // 국제화 메시지 조회
        String errorMessage = messageSource.getMessage(
            "error.email.duplicate",
            null,
            "이미 사용 중인 이메일입니다.",
            LocaleContextHolder.getLocale()
        );

        model.addAttribute("error", errorMessage);
        model.addAttribute("signupDto", new io.goorm.board.dto.SignupDto());

        return "auth/signup";
    }

    /**
     * InvalidCredentialsException 처리
     * 로그인 실패 시 로그인 폼으로 리다이렉트
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public String handleInvalidCredentialsException(InvalidCredentialsException e, Model model) {
        logger.warn("Invalid credentials: {}", e.getMessage());

        // 국제화 메시지 조회
        String errorMessage = messageSource.getMessage(
            "error.login.invalid",
            null,
            "이메일 또는 비밀번호가 올바르지 않습니다.",
            LocaleContextHolder.getLocale()
        );

        model.addAttribute("error", errorMessage);
        model.addAttribute("loginDto", new io.goorm.board.dto.LoginDto());

        return "auth/login";
    }

    /**
     * UserNotFoundException 처리
     * 사용자 조회 실패 시 메인 페이지로 리다이렉트
     */
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, Model model) {
        logger.warn("User not found: {}", e.getMessage());

        // 국제화 메시지 조회
        String errorMessage = messageSource.getMessage(
            "error.user.notfound",
            null,
            "사용자를 찾을 수 없습니다.",
            LocaleContextHolder.getLocale()
        );

        model.addAttribute("error", errorMessage);

        return "error/404";
    }

    /**
     * 기타 모든 예외 처리
     * 예상하지 못한 예외가 발생할 때 500 페이지로 이동
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        
        // 국제화 메시지 조회
        String errorMessage = messageSource.getMessage(
            "error.server.internal",
            null,
            LocaleContextHolder.getLocale()
        );
        
        model.addAttribute("error", errorMessage);
        
        // 개발 환경에서는 상세 에러 정보 표시
        if (logger.isDebugEnabled()) {
            model.addAttribute("exception", e.getClass().getSimpleName());
            model.addAttribute("message", e.getMessage());
        }
        
        return "error/500";
    }
}