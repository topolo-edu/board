package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.response.LoginResponse;
import io.goorm.board.dto.response.LogoutResponse;
import io.goorm.board.dto.response.SessionResponse;
import io.goorm.board.dto.response.UserResponse;
import io.goorm.board.entity.User;
import io.goorm.board.exception.UnauthenticatedException;
import io.goorm.board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/responseentity/auth")
@RequiredArgsConstructor
public class ResponseEntityAuthController {

    private final UserService userService;
    private final MessageSource messageSource;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginDto loginRequest,
                                                           HttpServletRequest request) {
        // UserService를 통한 인증
        User user = userService.authenticate(loginRequest);

        // SecurityContext에 인증 정보 설정
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 세션에 SecurityContext 저장
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 응답 생성
        UserResponse userResponse = UserResponse.from(user);
        String sessionId = request.getSession().getId();
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.login.success", null, locale);

        LoginResponse response = LoginResponse.builder()
                .sessionId(sessionId)
                .user(userResponse)
                .message(message)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(HttpServletRequest request) {
        // 세션 무효화 및 SecurityContext 정리
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("auth.logout.success", null, locale);

        LogoutResponse response = LogoutResponse.builder()
                .success(true)
                .message(message)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<SessionResponse>> checkSession(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = (User) auth.getPrincipal();
            UserResponse userResponse = UserResponse.from(user);

            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("auth.session.check.success", null, locale);

            SessionResponse response = SessionResponse.builder()
                    .authenticated(true)
                    .sessionId(request.getSession().getId())
                    .user(userResponse)
                    .message(message)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
        } else {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage("auth.user.unauthenticated", null, locale);

            throw new UnauthenticatedException(message);
        }
    }
}