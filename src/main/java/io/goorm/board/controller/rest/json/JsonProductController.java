package io.goorm.board.controller.rest.json;

import io.goorm.board.dto.product.ProductCreateDto;
import io.goorm.board.dto.product.ProductDto;
import io.goorm.board.dto.product.ProductSearchDto;
import io.goorm.board.dto.product.ProductUpdateDto;
import io.goorm.board.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/json/products")
@RequiredArgsConstructor
public class JsonProductController {

    private final ProductService productService;

    // 상품 목록 조회 (페이징 + 검색)
    @GetMapping
    public Page<ProductDto> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String category) {

        ProductSearchDto searchDto = ProductSearchDto.builder()
                .page(page)
                .size(size)
                .name(name)
                .code(code)
                .category(category)
                .build();

        return productService.findAll(searchDto);
    }

    // 상품 상세 조회
    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return productService.findById(id);
    }

    // 상품 등록
    @PostMapping
    public ProductDto createProduct(@Valid @RequestBody ProductCreateDto createDto) {
        return productService.create(createDto);
    }

    // 상품 수정
    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id,
                                   @Valid @RequestBody ProductUpdateDto updateDto) {

        // ID 설정
        updateDto.setProductSeq(id);
        return productService.update(updateDto);
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProduct(@PathVariable Long id) {
        productService.delete(id); // void 반환이므로 컨트롤러에서 응답 생성

        // 삭제 성공 응답
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "상품이 성공적으로 삭제되었습니다.");
        response.put("deletedProductId", id);
        return response;
    }

    // 상품 코드 중복 확인
    @GetMapping("/check-code/{code}")
    public Map<String, Object> checkCodeDuplicate(@PathVariable String code) {
        boolean isDuplicate = productService.isCodeDuplicate(code);

        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("isDuplicate", isDuplicate);
        response.put("available", !isDuplicate);
        return response;
    }
}