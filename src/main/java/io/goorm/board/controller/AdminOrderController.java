package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(@ModelAttribute OrderSearchDto searchDto, Model model) {
        // 관리자는 모든 발주 보기 + 회사 검색 조건
        List<OrderDto> orders = orderService.findAll(searchDto);
        model.addAttribute("orders", orders);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("isAdmin", true); // 관리자 권한 플래그
        return "buyer/orders/list"; // 같은 템플릿 사용
    }

    @GetMapping("/{orderSeq}")
    public String detail(@PathVariable Long orderSeq, Model model) {
        OrderDto order = orderService.findById(orderSeq);
        model.addAttribute("order", order);
        model.addAttribute("isAdmin", true); // 관리자 권한 플래그
        return "buyer/orders/detail"; // 같은 템플릿 사용
    }

    @PostMapping("/{orderSeq}/complete-delivery")
    public String completeDelivery(@PathVariable Long orderSeq,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.completeDelivery(orderSeq);
            redirectAttributes.addFlashAttribute("successMessage", "배송이 완료처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}