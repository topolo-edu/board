package io.goorm.board.controller.rest.jwt;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.request.JwtRefreshRequest;
import io.goorm.board.dto.response.JwtLoginResponse;
import io.goorm.board.dto.response.JwtRefreshResponse;
import io.goorm.board.dto.response.UserResponse;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import io.goorm.board.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/jwt/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API (JWT)", description = "JWT 기반 사용자 인증 관련 API")
public class JwtAuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Operation(summary = "JWT 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = JwtLoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtLoginResponse>> login(
            @Parameter(description = "로그인 정보") @Valid @RequestBody LoginDto loginRequest) {

        try {
            // Spring Security를 통한 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            // 인증된 사용자 정보 조회
            User user = userService.findByEmail(loginRequest.getEmail());

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            // 응답 생성
            JwtLoginResponse response = JwtLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .user(UserResponse.from(user))
                    .build();

            ApiResponse<JwtLoginResponse> apiResponse = ApiResponse.<JwtLoginResponse>builder()
                    .success(true)
                    .message("로그인 성공")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("🎯 JWT 로그인 성공: {} | Access Token: {}...{} | Refresh Token: {}...{}",
                    user.getEmail(),
                    accessToken.substring(0, 20),
                    accessToken.substring(accessToken.length() - 10),
                    refreshToken.substring(0, 20),
                    refreshToken.substring(refreshToken.length() - 10));
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("❌ JWT 로그인 실패: {} | 요청 이메일: {}", e.getMessage(), loginRequest.getEmail());

            ApiResponse<JwtLoginResponse> apiResponse = ApiResponse.<JwtLoginResponse>builder()
                    .success(false)
                    .message("로그인 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = JwtRefreshResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 검증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtRefreshResponse>> refresh(
            @Parameter(description = "갱신 요청 정보") @Valid @RequestBody JwtRefreshRequest request) {

        try {
            String refreshToken = request.getRefreshToken();

            // Refresh Token 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
            }

            // 토큰에서 사용자 이메일 추출
            String email = jwtUtil.getEmailFromToken(refreshToken);

            // 새로운 Access Token 생성
            String newAccessToken = jwtUtil.generateAccessToken(email);

            JwtRefreshResponse response = JwtRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .build();

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(true)
                    .message("토큰 갱신 성공")
                    .data(response)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 토큰 갱신 성공: {}", email);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("JWT 토큰 갱신 실패: {}", e.getMessage());

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(false)
                    .message("토큰 갱신 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "일반 로그아웃", description = "localStorage/sessionStorage 방식용 로그아웃 (클라이언트에서 토큰을 삭제하세요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-client")
    public ResponseEntity<ApiResponse<String>> logoutClient(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        try {
            // localStorage/sessionStorage 방식은 클라이언트에서 토큰 삭제
            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("로그아웃되었습니다. 클라이언트에서 토큰을 삭제해주세요.")
                    .data("클라이언트에서 localStorage 또는 sessionStorage의 토큰을 삭제하세요.")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 클라이언트 로그아웃: {}", user != null ? user.getEmail() : "unknown");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("JWT 클라이언트 로그아웃 실패: {}", e.getMessage());

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("로그아웃 처리 중 오류가 발생했습니다.")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인된 사용자의 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        try {
            if (user == null) {
                throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다");
            }

            UserResponse userResponse = UserResponse.from(user);

            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("사용자 정보 조회 성공")
                    .data(userResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("JWT 사용자 정보 조회: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("JWT 사용자 정보 조회 실패: {}", e.getMessage());

            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("사용자 정보 조회 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(response);
        }
    }

    // ===============================
    // httpOnly Cookie 전용 API들
    // ===============================

    @Operation(summary = "httpOnly Cookie 토큰 설정", description = "로그인 후 JWT 토큰을 httpOnly Cookie로 설정합니다")
    @PostMapping("/set-cookie-tokens")
    public ResponseEntity<ApiResponse<String>> setCookieTokens(
            @RequestBody Map<String, String> tokens,
            HttpServletResponse response) {

        try {
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            if (accessToken == null || refreshToken == null) {
                throw new RuntimeException("토큰이 제공되지 않았습니다");
            }

            // Access Token Cookie 설정
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false); // 개발환경에서는 false, 운영환경에서는 true
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) jwtUtil.getAccessTokenExpirationInSeconds());
            response.addCookie(accessTokenCookie);

            // Refresh Token Cookie 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // 개발환경에서는 false, 운영환경에서는 true
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) jwtUtil.getRefreshTokenExpirationInSeconds());
            response.addCookie(refreshTokenCookie);

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .success(true)
                    .message("httpOnly Cookie 토큰이 설정되었습니다")
                    .data("쿠키가 성공적으로 설정되었습니다")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("httpOnly Cookie 토큰 설정 완료");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("httpOnly Cookie 토큰 설정 실패: {}", e.getMessage());

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .success(false)
                    .message("Cookie 토큰 설정 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(400).body(apiResponse);
        }
    }

    @Operation(summary = "httpOnly Cookie 토큰 갱신", description = "httpOnly Cookie의 Refresh Token으로 새로운 토큰을 발급받습니다")
    @PostMapping("/refresh-cookie")
    public ResponseEntity<ApiResponse<JwtRefreshResponse>> refreshCookie(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Cookie에서 Refresh Token 추출
            String refreshToken = getCookieValue(request, "refreshToken");
            if (refreshToken == null) {
                throw new RuntimeException("Refresh Token Cookie가 없습니다");
            }

            // Refresh Token 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
            }

            // 토큰에서 사용자 이메일 추출
            String email = jwtUtil.getEmailFromToken(refreshToken);

            // 새로운 토큰 생성
            String newAccessToken = jwtUtil.generateAccessToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            // 새로운 Cookie 설정
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge((int) jwtUtil.getAccessTokenExpirationInSeconds());
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) jwtUtil.getRefreshTokenExpirationInSeconds());
            response.addCookie(refreshTokenCookie);

            JwtRefreshResponse jwtResponse = JwtRefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())
                    .build();

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(true)
                    .message("httpOnly Cookie 토큰이 갱신되었습니다")
                    .data(jwtResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("httpOnly Cookie 토큰 갱신 성공: {}", email);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("httpOnly Cookie 토큰 갱신 실패: {}", e.getMessage());

            ApiResponse<JwtRefreshResponse> apiResponse = ApiResponse.<JwtRefreshResponse>builder()
                    .success(false)
                    .message("Cookie 토큰 갱신 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "토큰 유효성 확인", description = "현재 토큰의 유효성을 확인합니다")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkToken(
            HttpServletRequest request,
            @AuthenticationPrincipal User user) {

        try {
            Map<String, Object> result = new HashMap<>();

            if (user != null) {
                result.put("valid", true);
                result.put("email", user.getEmail());
                result.put("authenticated", true);
            } else {
                result.put("valid", false);
                result.put("authenticated", false);
            }

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("토큰 상태 확인 완료")
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("토큰 확인 실패: {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("authenticated", false);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("토큰 확인 실패: " + e.getMessage())
                    .data(result)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(401).body(apiResponse);
        }
    }

    @Operation(summary = "httpOnly Cookie 정보 조회", description = "현재 설정된 httpOnly Cookie 정보를 조회합니다")
    @GetMapping("/cookie-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCookieInfo(
            HttpServletRequest request) {

        try {
            Map<String, Object> cookieInfo = new HashMap<>();

            String accessToken = getCookieValue(request, "accessToken");
            String refreshToken = getCookieValue(request, "refreshToken");

            Map<String, Object> accessTokenInfo = new HashMap<>();
            if (accessToken != null && jwtUtil.validateToken(accessToken)) {
                String email = jwtUtil.getEmailFromToken(accessToken);
                accessTokenInfo.put("user", email);
                accessTokenInfo.put("expiry", "서버에서 관리됨");
                accessTokenInfo.put("valid", true);
            } else {
                accessTokenInfo.put("valid", false);
            }

            Map<String, Object> refreshTokenInfo = new HashMap<>();
            if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                String email = jwtUtil.getEmailFromToken(refreshToken);
                refreshTokenInfo.put("user", email);
                refreshTokenInfo.put("expiry", "서버에서 관리됨");
                refreshTokenInfo.put("valid", true);
            } else {
                refreshTokenInfo.put("valid", false);
            }

            cookieInfo.put("accessToken", accessTokenInfo);
            cookieInfo.put("refreshToken", refreshTokenInfo);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Cookie 정보 조회 성공")
                    .data(cookieInfo)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("Cookie 정보 조회 실패: {}", e.getMessage());

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Cookie 정보 조회 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    @Operation(summary = "httpOnly Cookie 로그아웃", description = "httpOnly Cookie를 삭제합니다")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutCookie(
            HttpServletResponse response,
            @AuthenticationPrincipal User user) {

        try {
            // Access Token Cookie 삭제
            Cookie accessTokenCookie = new Cookie("accessToken", "");
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0); // 즉시 만료
            response.addCookie(accessTokenCookie);

            // Refresh Token Cookie 삭제
            Cookie refreshTokenCookie = new Cookie("refreshToken", "");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // 즉시 만료
            response.addCookie(refreshTokenCookie);

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .success(true)
                    .message("httpOnly Cookie가 삭제되었습니다")
                    .data("로그아웃 완료")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("httpOnly Cookie 로그아웃: {}", user != null ? user.getEmail() : "unknown");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            log.error("httpOnly Cookie 로그아웃 실패: {}", e.getMessage());

            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .success(false)
                    .message("로그아웃 처리 중 오류가 발생했습니다")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(500).body(apiResponse);
        }
    }

    /**
     * Cookie에서 특정 값을 추출하는 헬퍼 메서드
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }
}