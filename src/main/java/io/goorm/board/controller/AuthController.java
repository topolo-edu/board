package io.goorm.board.controller;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final MessageSource messageSource;
    
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "auth/signup";
    }
    
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupDto signupDto, 
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Locale locale) {
        
        if (result.hasErrors()) {
            return "auth/signup";
        }
        
        try {
            User user = userService.signup(signupDto);
            String message = messageSource.getMessage("flash.user.created", null, locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/";
        } catch (Exception e) {
            result.reject("signup.failed", e.getMessage());
            return "auth/signup";
        }
    }
    
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "auth/login";
    }
    
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginDto loginDto,
                       BindingResult result,
                       HttpSession session,
                       RedirectAttributes redirectAttributes,
                       Locale locale) {
        
        if (result.hasErrors()) {
            return "auth/login";
        }
        
        try {
            User user = userService.authenticate(loginDto);
            
            // 세션에 사용자 정보 저장
            session.setAttribute("user", user);
            
            String message = messageSource.getMessage("flash.login.success", null, "로그인되었습니다.", locale);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/";
        } catch (Exception e) {
            result.reject("login.failed", e.getMessage());
            return "auth/login";
        }
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Locale locale) {
        
        session.invalidate();
        
        String message = messageSource.getMessage("flash.logout.success", null, "로그아웃되었습니다.", locale);
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/";
    }
}