package io.goorm.board.service.impl;

import io.goorm.board.dto.product.ProductCreateDto;
import io.goorm.board.dto.product.ProductDto;
import io.goorm.board.dto.product.ProductExcelDto;
import io.goorm.board.dto.product.ProductSearchDto;
import io.goorm.board.dto.product.ProductUpdateDto;
import io.goorm.board.entity.Product;
import io.goorm.board.exception.product.ProductCodeDuplicateException;
import io.goorm.board.exception.product.ProductNotFoundException;
import io.goorm.board.exception.product.ProductValidationException;
import io.goorm.board.mapper.ProductMapper;
import io.goorm.board.service.ExcelExportService;
import io.goorm.board.util.ExcelUtil.CellType;
import io.goorm.board.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.goorm.board.service.ProductService;

/**
 * 상품 서비스 구현체
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceMyBatisImpl implements ProductService {

    private final ProductMapper productMapper;
    private final FileUploadUtil fileUploadUtil;
    private final ExcelExportService excelExportService;

    @Override
    @Transactional
    public ProductDto create(ProductCreateDto createDto) {
        log.debug("Creating product with code: {}", createDto.getCode());

        // 코드 중복 확인
        if (productMapper.existsByCode(createDto.getCode())) {
            throw new ProductCodeDuplicateException(createDto.getCode());
        }

        // 가격 검증
        if (!createDto.isPriceValid()) {
            throw new ProductValidationException("판매가는 원가보다 크거나 같아야 합니다.");
        }

        // 이미지 업로드 처리
        String imageUrl = null;
        if (createDto.hasImageFile()) {
            try {
                imageUrl = fileUploadUtil.uploadProductImage(createDto.getImageFile());
            } catch (Exception e) {
                log.error("Failed to upload product image", e);
                throw new ProductValidationException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }

        // Entity 생성
        Product product = Product.builder()
                .code(createDto.getCode())
                .name(createDto.getName())
                .description(createDto.getDescription())
                .categorySeq(createDto.getCategorySeq())
                .supplierSeq(createDto.getSupplierSeq())
                .unitPrice(createDto.getUnitPrice())
                .unitCost(createDto.getUnitCost())
                .unit(createDto.getUnit())
                .sku(createDto.getSku())
                .barcode(createDto.getBarcode())
                .weight(createDto.getWeight())
                .dimensions(createDto.getDimensions())
                .imageUrl(imageUrl)
                .status(createDto.getStatus())
                .build();

        // 저장
        int result = productMapper.insert(product);
        if (result != 1) {
            throw new RuntimeException("상품 등록에 실패했습니다.");
        }

        log.info("Product created successfully with seq: {}", product.getProductSeq());
        return findById(product.getProductSeq());
    }

    @Override
    @Transactional
    public ProductDto update(ProductUpdateDto updateDto) {
        log.debug("Updating product with seq: {}", updateDto.getProductSeq());

        // 존재하는 상품인지 확인
        Product existingProduct = productMapper.findById(updateDto.getProductSeq())
                .orElseThrow(() -> new ProductNotFoundException(updateDto.getProductSeq()));

        // 코드 중복 확인 (자신 제외)
        if (productMapper.existsByCodeAndNotSeq(updateDto.getCode(), updateDto.getProductSeq())) {
            throw new ProductCodeDuplicateException(updateDto.getCode());
        }

        // 가격 검증
        if (!updateDto.isPriceValid()) {
            throw new ProductValidationException("판매가는 원가보다 크거나 같아야 합니다.");
        }

        // 이미지 처리
        String imageUrl = existingProduct.getImageUrl();

        // 이미지 삭제 요청시
        if (updateDto.isDeleteImageRequested()) {
            if (imageUrl != null) {
                try {
                    fileUploadUtil.deleteFile(imageUrl);
                } catch (Exception e) {
                    log.warn("Failed to delete existing image: {}", imageUrl, e);
                }
            }
            imageUrl = null;
        }

        // 새 이미지 업로드시
        if (updateDto.hasImageFile()) {
            // 기존 이미지 삭제
            if (imageUrl != null) {
                try {
                    fileUploadUtil.deleteFile(imageUrl);
                } catch (Exception e) {
                    log.warn("Failed to delete existing image: {}", imageUrl, e);
                }
            }

            // 새 이미지 업로드
            try {
                imageUrl = fileUploadUtil.uploadProductImage(updateDto.getImageFile());
            } catch (Exception e) {
                log.error("Failed to upload product image", e);
                throw new ProductValidationException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        }

        // Entity 업데이트
        Product product = Product.builder()
                .productSeq(updateDto.getProductSeq())
                .code(updateDto.getCode())
                .name(updateDto.getName())
                .description(updateDto.getDescription())
                .categorySeq(updateDto.getCategorySeq())
                .supplierSeq(updateDto.getSupplierSeq())
                .unitPrice(updateDto.getUnitPrice())
                .unitCost(updateDto.getUnitCost())
                .unit(updateDto.getUnit())
                .sku(updateDto.getSku())
                .barcode(updateDto.getBarcode())
                .weight(updateDto.getWeight())
                .dimensions(updateDto.getDimensions())
                .imageUrl(imageUrl)
                .status(updateDto.getStatus())
                .createdAt(existingProduct.getCreatedAt())
                .build();

        // 저장
        int result = productMapper.update(product);
        if (result != 1) {
            throw new RuntimeException("상품 수정에 실패했습니다.");
        }

        log.info("Product updated successfully with seq: {}", updateDto.getProductSeq());
        return findById(updateDto.getProductSeq());
    }

    @Override
    @Transactional
    public void delete(Long productSeq) {
        log.debug("Deleting product with seq: {}", productSeq);

        // 존재하는 상품인지 확인
        Product product = productMapper.findById(productSeq)
                .orElseThrow(() -> new ProductNotFoundException(productSeq));

        // 이미지 파일 삭제
        if (product.getImageUrl() != null) {
            try {
                fileUploadUtil.deleteFile(product.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete product image: {}", product.getImageUrl(), e);
            }
        }

        // 상품 삭제
        int result = productMapper.delete(productSeq);
        if (result != 1) {
            throw new RuntimeException("상품 삭제에 실패했습니다.");
        }

        log.info("Product deleted successfully with seq: {}", productSeq);
    }

    @Override
    public ProductDto findById(Long productSeq) {
        return productMapper.findDtoById(productSeq)
                .orElseThrow(() -> new ProductNotFoundException(productSeq));
    }

    @Override
    public Page<ProductDto> findAll(ProductSearchDto searchDto) {
        List<ProductDto> products = productMapper.findAll(searchDto);
        int total = productMapper.count(searchDto);

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, searchDto.getPage() - 1),
                searchDto.getSize()
        );

        return new PageImpl<>(products, pageRequest, total);
    }

    @Override
    public boolean isCodeDuplicate(String code) {
        return productMapper.existsByCode(code);
    }

    @Override
    public boolean isCodeDuplicate(String code, Long excludeProductSeq) {
        return productMapper.existsByCodeAndNotSeq(code, excludeProductSeq);
    }

    @Override
    public List<ProductDto> findByStatus(String status) {
        return productMapper.findByStatus(status);
    }

    @Override
    public List<ProductDto> findSellableProducts() {
        return productMapper.findSellableProducts();
    }

    @Override
    public List<ProductDto> findRecentProducts(int limit) {
        return productMapper.findRecentProducts(limit);
    }

    @Override
    public int countByCategory(Long categorySeq) {
        return productMapper.countByCategory(categorySeq);
    }

    @Override
    public int countBySupplier(Long supplierSeq) {
        return productMapper.countBySupplier(supplierSeq);
    }

    @Override
    public List<ProductDto> findAllForExport(ProductSearchDto searchDto) {
        log.debug("Finding all products for export with search: {}", searchDto);

        // 페이징 없이 전체 조회
        searchDto.setPage(0);
        searchDto.setSize(Integer.MAX_VALUE);
        Page<ProductDto> productPage = findAll(searchDto);
        return productPage.getContent();
    }

    @Override
    public byte[] exportToExcel(ProductSearchDto searchDto) {
        log.debug("Exporting products to Excel with search: {}", searchDto);

        // Excel 내보내기용 상품 목록 조회
        List<ProductDto> products = findAllForExport(searchDto);

        // ProductDto를 ProductExcelDto로 변환
        List<ProductExcelDto> excelProducts = products.stream()
                .map(this::convertToExcelDto)
                .toList();

        // Excel 생성
        String[] headers = {
            "상품명", "상품코드", "카테고리", "공급업체",
            "판매가", "원가", "단위", "상태",
            "SKU", "바코드", "등록일"
        };

        // 컬럼 타입 정의
        CellType[] columnTypes = {
            CellType.STRING,  // 상품명
            CellType.STRING,  // 상품코드
            CellType.STRING,  // 카테고리
            CellType.STRING,  // 공급업체
            CellType.NUMERIC, // 판매가
            CellType.NUMERIC, // 원가
            CellType.STRING,  // 단위
            CellType.STRING,  // 상태
            CellType.STRING,  // SKU
            CellType.STRING,  // 바코드 (텍스트로 처리)
            CellType.STRING   // 등록일
        };

        return excelExportService.exportToExcelWithTypes("상품목록", headers, excelProducts, this::mapToRowDataWithTypes,
                columnTypes, LocaleContextHolder.getLocale());
    }

    /**
     * ProductDto를 ProductExcelDto로 변환
     */
    private ProductExcelDto convertToExcelDto(ProductDto product) {
        return ProductExcelDto.builder()
                .name(product.getName())
                .code(product.getCode())
                .categoryName(product.getCategoryName())
                .supplierName(product.getSupplierName())
                .unitPrice(product.getUnitPrice())
                .unitCost(product.getUnitCost())
                .unit(product.getUnit() != null ? product.getUnit().getDisplayName() : "")
                .status(product.getStatus() != null ? product.getStatus().getDisplayName() : "")
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .createdAt(product.getCreatedAt())
                .build();
    }

    /**
     * ProductExcelDto를 Excel 행 데이터로 변환 (문자열 배열)
     */
    private String[] mapToRowData(ProductExcelDto product) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new String[] {
            product.getName(),
            product.getCode(),
            product.getCategoryName(),
            product.getSupplierName(),
            product.getUnitPrice() != null ? product.getUnitPrice().toString() : "",
            product.getUnitCost() != null ? product.getUnitCost().toString() : "",
            product.getUnit(),
            product.getStatus(),
            product.getSku(),
            product.getBarcode(),
            product.getCreatedAt() != null ? product.getCreatedAt().format(formatter) : ""
        };
    }

    /**
     * ProductExcelDto를 Excel 행 데이터로 변환 (타입별 Object 배열)
     */
    private Object[] mapToRowDataWithTypes(ProductExcelDto product) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new Object[] {
            product.getName(),                          // STRING
            product.getCode(),                          // STRING
            product.getCategoryName(),                  // STRING
            product.getSupplierName(),                  // STRING
            product.getUnitPrice(),                     // NUMERIC (BigDecimal)
            product.getUnitCost(),                      // NUMERIC (BigDecimal)
            product.getUnit(),                          // STRING
            product.getStatus(),                        // STRING
            product.getSku(),                           // STRING
            product.getBarcode(),                       // STRING (텍스트로 처리)
            product.getCreatedAt() != null ? product.getCreatedAt().format(formatter) : "" // STRING
        };
    }
}