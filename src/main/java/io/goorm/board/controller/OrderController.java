package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.ProductService;
import io.goorm.board.service.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/buyer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final DiscountService discountService;

    @GetMapping
    public String list(@AuthenticationPrincipal User user,
                      @ModelAttribute OrderSearchDto searchDto,
                      Model model) {
        // 바이어는 본인 회사 발주만 보기
        List<OrderDto> orders = orderService.findByCompany(user.getCompanySeq(), searchDto);
        model.addAttribute("orders", orders);
        model.addAttribute("searchDto", searchDto);
        return "buyer/orders/list";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("orderCreateDto", new OrderCreateDto());
        model.addAttribute("products", productService.findSellableProducts());

        // 할인율 정보 추가
        if (user.getCompanySeq() != null) {
            model.addAttribute("discountRate", discountService.calculateDiscountRate(user.getCompanySeq()));
        }

        return "buyer/orders/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute OrderCreateDto orderCreateDto,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findSellableProducts());

            // 오류 시에도 할인율 정보 추가
            if (user.getCompanySeq() != null) {
                model.addAttribute("discountRate", discountService.calculateDiscountRate(user.getCompanySeq()));
            }

            return "buyer/orders/form";
        }

        try {
            OrderDto order = orderService.createOrder(orderCreateDto, user);
            redirectAttributes.addFlashAttribute("successMessage", "발주가 성공적으로 등록되었습니다.");
            return "redirect:/buyer/orders/" + order.getOrderSeq();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productService.findSellableProducts());
            return "buyer/orders/form";
        }
    }

    @GetMapping("/{orderSeq}")
    public String detail(@PathVariable Long orderSeq, Model model) {
        OrderDto order = orderService.findById(orderSeq);
        model.addAttribute("order", order);
        return "buyer/orders/detail";
    }

}