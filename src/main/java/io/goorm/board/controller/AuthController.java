package io.goorm.board.controller;

import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
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
}