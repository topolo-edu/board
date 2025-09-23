package io.goorm.board.service;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderItemCreateDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Order;
import io.goorm.board.entity.OrderItem;
import io.goorm.board.entity.Product;
import io.goorm.board.entity.User;
import io.goorm.board.exception.InsufficientStockException;
import io.goorm.board.exception.OrderNotFoundException;
import io.goorm.board.mapper.OrderItemMapper;
import io.goorm.board.mapper.OrderMapper;
import io.goorm.board.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final DiscountService discountService;

    @Transactional
    public OrderDto createOrder(OrderCreateDto createDto, User user) {
        // 발주번호 생성
        String orderNumber = generateOrderNumber();

        // 할인율 계산
        BigDecimal discountRate = discountService.calculateDiscountRate(user.getCompanySeq());

        // 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(createDto);
        BigDecimal discountAmount = totalAmount.multiply(discountRate).divide(BigDecimal.valueOf(100));
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // 발주 등록
        Order order = Order.builder()
                .companySeq(user.getCompanySeq())
                .userSeq(user.getUserSeq())
                .orderNumber(orderNumber)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();

        order.approve("SYSTEM"); // 자동 승인
        orderMapper.insert(order);

        // 발주 상품 등록
        List<OrderItem> orderItems = createDto.getItems().stream()
                .map(itemDto -> {
                    BigDecimal lineAmount = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                    BigDecimal lineDiscountAmount = lineAmount.multiply(discountRate).divide(BigDecimal.valueOf(100));
                    BigDecimal lineTotal = lineAmount.subtract(lineDiscountAmount);

                    return OrderItem.builder()
                            .orderSeq(order.getOrderSeq())
                            .productSeq(itemDto.getProductSeq())
                            .quantity(itemDto.getQuantity())
                            .unitPrice(itemDto.getUnitPrice())
                            .discountRate(discountRate)
                            .discountAmount(lineDiscountAmount)
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        orderItemMapper.insertBatch(orderItems);

        return convertToDto(orderMapper.findById(order.getOrderSeq()).orElseThrow());
    }

    public List<OrderDto> findByCompany(Long companySeq, OrderSearchDto searchDto) {
        return orderMapper.findByCompanySeq(companySeq, searchDto).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<OrderDto> findAll(OrderSearchDto searchDto) {
        return orderMapper.findAll(searchDto).stream()
                .map(this::convertToDto)
                .toList();
    }

    public OrderDto findById(Long orderSeq) {
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException("발주를 찾을 수 없습니다."));

        OrderDto orderDto = convertToDto(order);

        // 발주 상품 목록 조회
        List<OrderItem> orderItems = orderItemMapper.findByOrderSeq(orderSeq);
        orderDto.setOrderItems(orderItems.stream()
                .map(this::convertToItemDto)
                .toList());

        return orderDto;
    }

    @Transactional
    public void completeDelivery(Long orderSeq) {
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException("발주를 찾을 수 없습니다."));

        order.completeDelivery();
        orderMapper.update(order);
    }


    private BigDecimal calculateTotalAmount(OrderCreateDto createDto) {
        return createDto.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = orderMapper.findMaxDailySequence(datePrefix) + 1;
        return String.format("%s%04d", datePrefix, sequence);
    }

    private OrderDto convertToDto(Order order) {
        return OrderDto.builder()
                .orderSeq(order.getOrderSeq())
                .companySeq(order.getCompanySeq())
                .companyName(order.getCompanyName())
                .userSeq(order.getUserSeq())
                .userName(order.getUserName())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .discountRate(order.getDiscountRate())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private io.goorm.board.dto.order.OrderItemDto convertToItemDto(OrderItem orderItem) {
        return io.goorm.board.dto.order.OrderItemDto.builder()
                .orderItemSeq(orderItem.getOrderItemSeq())
                .orderSeq(orderItem.getOrderSeq())
                .productSeq(orderItem.getProductSeq())
                .productName(orderItem.getProductName())
                .productCode(orderItem.getProductCode())
                .categoryName(orderItem.getCategoryName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .discountRate(orderItem.getDiscountRate())
                .discountAmount(orderItem.getDiscountAmount())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }
}