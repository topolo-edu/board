package io.goorm.board.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * 전역 예외 처리 클래스
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 게시글을 찾을 수 없을 때 (404 에러)
     */
    @ExceptionHandler(PostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handlePostNotFound(PostNotFoundException e) {
        log.error("게시글을 찾을 수 없습니다: {}", e.getMessage());
        
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", e.getMessage());
        return mav;
    }

    /**
     * 일반적인 서버 오류 (500 에러)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericError(Exception e) {
        log.error("서버 오류 발생: ", e);
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return mav;
    }
}