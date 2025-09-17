package io.goorm.board.service.impl;

import io.goorm.board.dto.category.CategoryCreateDto;
import io.goorm.board.dto.category.CategoryDto;
import io.goorm.board.dto.category.CategoryExcelDto;
import io.goorm.board.dto.category.CategorySearchDto;
import io.goorm.board.dto.category.CategoryUpdateDto;
import io.goorm.board.entity.Category;
import io.goorm.board.enums.CategoryStatus;
import io.goorm.board.exception.category.CategoryCodeDuplicateException;
import io.goorm.board.exception.category.CategoryNotFoundException;
import io.goorm.board.exception.category.CategoryValidationException;
import io.goorm.board.repository.CategoryRepository;
import io.goorm.board.service.CategoryService;
import io.goorm.board.service.ExcelExportService;
import io.goorm.board.util.ExcelUtil.CellType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 카테고리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExcelExportService excelExportService;

    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto createDto) {
        log.debug("Creating category: {}", createDto.getName());


        // 정렬순서 설정 (기존 최대값 + 1)
        Integer maxSortOrder = categoryRepository.findMaxSortOrder();
        int nextSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;

        // Entity 생성
        Category category = Category.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .sortOrder(nextSortOrder)
                .isActive(true)
                .build();

        // 저장
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with seq: {}", savedCategory.getCategorySeq());
        return convertToDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryUpdateDto updateDto) {
        log.debug("Updating category with seq: {}", updateDto.getCategorySeq());

        // 존재하는 카테고리인지 확인
        Category existingCategory = categoryRepository.findById(updateDto.getCategorySeq())
                .orElseThrow(() -> new CategoryNotFoundException(updateDto.getCategorySeq()));

        // Entity 업데이트
        existingCategory.updateBasicInfo(updateDto.getName(), updateDto.getDescription());

        // 저장
        Category updatedCategory = categoryRepository.save(existingCategory);

        log.info("Category updated successfully with seq: {}", updateDto.getCategorySeq());
        return convertToDto(updatedCategory);
    }

    @Override
    public CategoryDto findById(Long categorySeq) {
        Category category = categoryRepository.findById(categorySeq)
                .orElseThrow(() -> new CategoryNotFoundException(categorySeq));
        return convertToDto(category);
    }

    @Override
    public Page<CategoryDto> findAll(CategorySearchDto searchDto) {
        List<Category> categories = categoryRepository.findAllBySearchCondition(
                searchDto.getKeyword(),
                searchDto.getStatus() != null ? (searchDto.getStatus() == CategoryStatus.ACTIVE) : null
        );

        // 페이징 처리
        int offset = Math.max(0, (searchDto.getPage() - 1) * searchDto.getSize());
        int endIndex = Math.min(offset + searchDto.getSize(), categories.size());
        List<Category> pagedCategories = categories.subList(Math.min(offset, categories.size()), endIndex);

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, searchDto.getPage() - 1),
                searchDto.getSize()
        );

        List<CategoryDto> categoryDtos = pagedCategories.stream()
                .map(this::convertToDto)
                .toList();

        return new PageImpl<>(categoryDtos, pageRequest, categories.size());
    }

    @Override
    public List<CategoryDto> findAllForExport(CategorySearchDto searchDto) {
        log.debug("Finding all categories for export with search: {}", searchDto);

        List<Category> categories = categoryRepository.findAllBySearchCondition(
                searchDto.getKeyword(),
                searchDto.getStatus() != null ? (searchDto.getStatus() == CategoryStatus.ACTIVE) : null
        );

        return categories.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public byte[] exportToExcel(CategorySearchDto searchDto) {
        log.debug("Exporting categories to Excel with search: {}", searchDto);

        // Excel 내보내기용 카테고리 목록 조회
        List<CategoryDto> categories = findAllForExport(searchDto);

        // CategoryDto를 CategoryExcelDto로 변환
        List<CategoryExcelDto> excelCategories = categories.stream()
                .map(this::convertToExcelDto)
                .toList();

        // Excel 생성
        String[] headers = {
            "카테고리명", "설명", "상태", "정렬순서", "등록일"
        };

        // 컬럼 타입 정의
        CellType[] columnTypes = {
            CellType.STRING,  // 카테고리명
            CellType.STRING,  // 설명
            CellType.STRING,  // 상태
            CellType.NUMERIC, // 정렬순서
            CellType.STRING   // 등록일
        };

        return excelExportService.exportToExcelWithTypes("카테고리목록", headers, excelCategories,
                this::mapToRowDataWithTypes, columnTypes, LocaleContextHolder.getLocale());
    }

    @Override
    public List<CategoryDto> findAllActive() {
        List<Category> categories = categoryRepository.findByIsActiveOrderBySortOrderAsc(true);
        return categories.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<CategoryDto> findAllActiveOrSelected(Long selectedCategorySeq) {
        List<CategoryDto> activeCategories = findAllActive();

        // 선택된 카테고리가 비활성 상태인 경우 목록에 추가
        if (selectedCategorySeq != null) {
            boolean containsSelected = activeCategories.stream()
                    .anyMatch(category -> category.getCategorySeq().equals(selectedCategorySeq));

            if (!containsSelected) {
                try {
                    CategoryDto selectedCategory = findById(selectedCategorySeq);
                    activeCategories.add(selectedCategory);
                } catch (CategoryNotFoundException e) {
                    // 선택된 카테고리가 존재하지 않으면 무시
                    log.warn("Selected category not found: {}", selectedCategorySeq);
                }
            }
        }

        return activeCategories;
    }

    @Override
    @Transactional
    public void activate(Long categorySeq) {
        log.debug("Activating category with seq: {}", categorySeq);

        Category category = categoryRepository.findById(categorySeq)
                .orElseThrow(() -> new CategoryNotFoundException(categorySeq));

        category.activate();
        categoryRepository.save(category);

        log.info("Category activated successfully with seq: {}", categorySeq);
    }

    @Override
    @Transactional
    public void deactivate(Long categorySeq) {
        log.debug("Deactivating category with seq: {}", categorySeq);

        Category category = categoryRepository.findById(categorySeq)
                .orElseThrow(() -> new CategoryNotFoundException(categorySeq));

        category.deactivate();
        categoryRepository.save(category);

        log.info("Category deactivated successfully with seq: {}", categorySeq);
    }


    @Override
    public CategoryDto findBySeq(Long categorySeq) {
        return findById(categorySeq);
    }

    /**
     * Category 엔티티를 CategoryDto로 변환
     */
    private CategoryDto convertToDto(Category category) {
        return CategoryDto.builder()
                .categorySeq(category.getCategorySeq())
                .name(category.getName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * CategoryDto를 CategoryExcelDto로 변환
     */
    private CategoryExcelDto convertToExcelDto(CategoryDto category) {
        return CategoryExcelDto.builder()
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus() != null ? category.getStatus().getDisplayName() : "")
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .build();
    }

    /**
     * CategoryExcelDto를 Excel 행 데이터로 변환 (타입별 Object 배열)
     */
    private Object[] mapToRowDataWithTypes(CategoryExcelDto category) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new Object[] {
            category.getName(),                         // STRING
            category.getDescription(),                  // STRING
            category.getStatus(),                       // STRING
            category.getSortOrder(),                    // NUMERIC (Integer)
            category.getCreatedAt() != null ? category.getCreatedAt().format(formatter) : "" // STRING
        };
    }
}