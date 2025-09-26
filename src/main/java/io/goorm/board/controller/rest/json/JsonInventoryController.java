package io.goorm.board.controller.rest.json;

import io.goorm.board.entity.Inventory;
import io.goorm.board.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/json/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 관리자만 접근
public class JsonInventoryController {

    private final InventoryMapper inventoryMapper;

    // 특정 상품의 재고 조회
    @GetMapping("/product/{productSeq}")
    public Map<String, Object> getInventoryByProduct(@PathVariable Long productSeq) {
        Optional<Inventory> inventory = inventoryMapper.findByProductSeq(productSeq);

        Map<String, Object> response = new HashMap<>();
        if (inventory.isPresent()) {
            response.put("exists", true);
            response.put("inventory", inventory.get());
        } else {
            response.put("exists", false);
            response.put("message", "해당 상품의 재고 정보가 없습니다.");
            response.put("productSeq", productSeq);
        }
        return response;
    }

    // 여러 상품의 재고 일괄 조회
    @PostMapping("/products")
    public List<Inventory> getInventoriesByProducts(@RequestBody List<Long> productSeqs) {
        return inventoryMapper.findByProductSeqs(productSeqs);
    }
}