package io.goorm.board.service;

import io.goorm.board.dto.excel.StockReceivingDto;
import io.goorm.board.entity.Inventory;
import io.goorm.board.entity.Product;
import io.goorm.board.entity.User;
import io.goorm.board.exception.InsufficientStockException;
import io.goorm.board.mapper.InventoryMapper;
import io.goorm.board.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;

    /**
     * 재고 확인 및 검증
     */
    public void checkStock(Long productSeq, Integer quantity) {
        // 재고 체크
        Inventory inventory = inventoryMapper.findByProductSeq(productSeq)
                .orElse(null);

        if (inventory == null || !inventory.canOrder(quantity)) {
            // 상품명 조회 (예외 메시지용)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            int available = (inventory != null && inventory.getAvailableStock() != null)
                    ? inventory.getAvailableStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }
    }

    /**
     * 재고 차감 (주문 승인 시)
     */
    @Transactional
    public void decreaseStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.decreaseStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 차감 실패 (재고 부족 또는 상품 없음)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            Inventory inventory = inventoryMapper.findByProductSeq(productSeq).orElse(null);
            int available = (inventory != null && inventory.getCurrentStock() != null)
                    ? inventory.getCurrentStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }

        log.info("재고 차감 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 재고 예약 (주문 시)
     */
    @Transactional
    public void reserveStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.reserveStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 예약 실패 (재고 부족 또는 상품 없음)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            Inventory inventory = inventoryMapper.findByProductSeq(productSeq).orElse(null);
            int available = (inventory != null && inventory.getAvailableStock() != null)
                    ? inventory.getAvailableStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }

        log.info("재고 예약 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 재고 소모 처리 (배송 완료 시)
     */
    @Transactional
    public void consumeStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.consumeStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 소모 실패
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            throw new InsufficientStockException(productName, quantity, 0);
        }

        log.info("재고 소모 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 엑셀 입고 처리
     */
    @Transactional
    public List<String> processStockReceiving(List<StockReceivingDto> stockList, User user) {
        List<String> errors = new ArrayList<>();

        for (StockReceivingDto dto : stockList) {
            try {
                // 1. 상품명+카테고리로 상품 찾기
                Product product = findProductByNameAndCategory(dto.getProductName(), dto.getCategoryName());
                if (product == null) {
                    errors.add(String.format("%d행: 상품을 찾을 수 없습니다 - %s (%s)",
                            dto.getRowNumber(), dto.getProductName(), dto.getCategoryName()));
                    continue;
                }
                dto.setProductSeq(product.getProductSeq());

                // 2. 재고 업데이트
                updateStock(product.getProductSeq(), dto.getQuantity());

                log.info("입고 처리 완료 - {}행: {} ({}) +{}",
                        dto.getRowNumber(), dto.getProductName(), dto.getCategoryName(), dto.getQuantity());

            } catch (Exception e) {
                String errorMsg = String.format("%d행: %s", dto.getRowNumber(), e.getMessage());
                errors.add(errorMsg);
                log.error("입고 처리 실패 - {}", errorMsg, e);
            }
        }

        log.info("엑셀 입고 처리 완료 - 총 {}건, 성공 {}건, 실패 {}건",
                stockList.size(), stockList.size() - errors.size(), errors.size());

        return errors;
    }

    /**
     * 상품명과 카테고리명으로 상품 찾기
     */
    private Product findProductByNameAndCategory(String productName, String categoryName) {
        Optional<Product> productOpt = productMapper.findByNameAndCategory(productName, categoryName);
        return productOpt.orElse(null);
    }

    /**
     * 재고 업데이트 (입고 처리)
     */
    @Transactional
    public void updateStock(Long productSeq, Integer quantity) {
        // 기존 재고 조회
        Optional<Inventory> existingInventory = inventoryMapper.findByProductSeq(productSeq);

        if (existingInventory.isPresent()) {
            // 기존 재고에 추가
            int updatedRows = inventoryMapper.increaseStock(productSeq, quantity);
            if (updatedRows == 0) {
                throw new RuntimeException("재고 업데이트에 실패했습니다.");
            }
            log.debug("기존 재고 업데이트 - 상품: {}, 추가수량: {}", productSeq, quantity);
        } else {
            // 새로운 재고 생성
            Inventory newInventory = Inventory.builder()
                    .productSeq(productSeq)
                    .currentStock(quantity)
                    .reservedStock(0)
                    .build();

            inventoryMapper.insert(newInventory);
            log.debug("신규 재고 생성 - 상품: {}, 초기수량: {}", productSeq, quantity);
        }
    }

}