package io.goorm.board.controller;

import io.goorm.board.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 바이어 전용 컨트롤러
 */
@Controller
@RequestMapping("/buyer")
public class BuyerController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("pageTitle", "바이어 대시보드");
        model.addAttribute("user", user);
        return "buyer/dashboard";
    }

    @GetMapping("/orders")
    public String orderList(Model model) {
        model.addAttribute("pageTitle", "발주 관리");
        return "buyer/orders/list";
    }
}