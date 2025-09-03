package io.goorm.board.controller;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final UserService userService;
    private final MessageSource messageSource;
    
    @Autowired
    public AuthController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }
    
    // === 로그인 관련 ===
    
    /**
     * 로그인 페이지 표시
     */
    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }
    
    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginDto loginDto,
                       BindingResult bindingResult,
                       HttpSession session,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        // 1. 폼 검증 오류 확인
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        
        try {
            // 2. 인증 처리
            User authenticatedUser = userService.authenticate(loginDto);
            
            // 3. 세션에 사용자 정보 저장
            session.setAttribute("user", authenticatedUser);
            
            // 4. 로그인 성공 메시지와 함께 메인 페이지로 리다이렉트
            String welcomeMessage = authenticatedUser.getDisplayName() + messageSource.getMessage("flash.auth.login.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("message", welcomeMessage);
            return "redirect:/";
            
        } catch (IllegalArgumentException e) {
            // 5. 인증 실패 시 에러 메시지 표시
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }
    
    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // 1. 세션 무효화
        session.invalidate();
        
        // 2. 로그아웃 메시지와 함께 메인 페이지로 리다이렉트
        redirectAttributes.addFlashAttribute("message", messageSource.getMessage("flash.auth.logout.success", null, LocaleContextHolder.getLocale()));
        return "redirect:/";
    }
    
    // === 회원가입 관련 ===
    
    /**
     * 회원가입 페이지 표시
     */
    @GetMapping("/signup")
    public String signupForm(Model model, HttpSession session) {
        // 이미 로그인된 사용자는 메인 페이지로 리다이렉트
        if (session.getAttribute("user") != null) {
            return "redirect:/";
        }
        
        model.addAttribute("signupDto", new SignupDto());
        return "auth/signup";
    }
    
    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupDto signupDto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        // 1. 폼 검증 오류 확인
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        
        try {
            // 2. 회원가입 처리
            userService.signup(signupDto);
            
            // 3. 성공 메시지와 함께 로그인 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", messageSource.getMessage("flash.auth.signup.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/auth/login";
            
        } catch (IllegalArgumentException e) {
            // 4. 회원가입 실패 시 에러 메시지 표시
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }
    
    // === 프로필 관련 ===
    
    /**
     * 프로필 페이지 표시
     */
    @GetMapping("/profile")
    public String profileForm(Model model, HttpSession session) {
        // 1. 로그인 확인
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // 2. 최신 사용자 정보 조회 (프로필 수정 후 새로고침을 위해)
        User user = userService.findById(currentUser.getId())
            .orElse(currentUser);
        
        // 3. DTO 생성 및 기본값 설정
        ProfileUpdateDto profileUpdateDto = new ProfileUpdateDto();
        profileUpdateDto.setEmail(user.getEmail());
        profileUpdateDto.setDisplayName(user.getDisplayName());
        
        model.addAttribute("user", user);
        model.addAttribute("profileUpdateDto", profileUpdateDto);
        return "auth/profile";
    }
    
    /**
     * 프로필 업데이트 처리
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute ProfileUpdateDto profileUpdateDto,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        // 1. 로그인 확인
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }
        
        // 2. 폼 검증 오류 확인
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", currentUser);
            return "auth/profile";
        }
        
        try {
            // 3. 프로필 업데이트 처리
            User updatedUser = userService.updateProfile(currentUser.getId(), profileUpdateDto);
            
            // 4. 세션의 사용자 정보 업데이트
            session.setAttribute("user", updatedUser);
            
            // 5. 성공 메시지와 함께 프로필 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", messageSource.getMessage("flash.auth.profile.updated", null, LocaleContextHolder.getLocale()));
            return "redirect:/auth/profile";
            
        } catch (IllegalArgumentException e) {
            // 6. 업데이트 실패 시 에러 메시지 표시
            model.addAttribute("user", currentUser);
            model.addAttribute("error", e.getMessage());
            return "auth/profile";
        }
    }
}