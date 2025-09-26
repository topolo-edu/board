package io.goorm.board.controller.rest.json;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/json/orders")
@RequiredArgsConstructor
public class JsonOrderController {

    private final OrderService orderService;

    // 전체 주문 목록 조회 (관리자만)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDto> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long companySeq) {

        OrderSearchDto searchDto = OrderSearchDto.builder()
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .companySeq(companySeq)
                .build();

        return orderService.findAll(searchDto);
    }

    // 회사별 주문 목록 조회 (바이어)
    @GetMapping("/company/{companySeq}")
    @PreAuthorize("hasRole('BUYER')")
    public List<OrderDto> getOrdersByCompany(
            @PathVariable Long companySeq,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal User user) {

        OrderSearchDto searchDto = OrderSearchDto.builder()
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .companySeq(companySeq)
                .build();

        return orderService.findByCompany(companySeq, searchDto);
    }

    // 내 주문 목록 조회 (현재 로그인 사용자)
    @GetMapping("/my")
    public List<OrderDto> getMyOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal User user) {

        // 사용자의 회사 기준으로 조회
        OrderSearchDto searchDto = OrderSearchDto.builder()
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .companySeq(user.getCompany() != null ? user.getCompany().getSeq() : null)
                .build();

        return orderService.findByCompany(user.getCompany().getSeq(), searchDto);
    }

    // 주문 상세 조회
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    // 주문 생성 (바이어만)
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public OrderDto createOrder(@Valid @RequestBody OrderCreateDto createDto,
                               @AuthenticationPrincipal User user) {

        return orderService.createOrder(createDto, user);
    }

}