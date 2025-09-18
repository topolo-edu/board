package io.goorm.board.service.impl;

import io.goorm.board.dto.category.CategoryCreateDto;
import io.goorm.board.dto.category.CategoryDto;
import io.goorm.board.dto.category.CategoryExcelDto;
import io.goorm.board.dto.category.CategorySearchDto;
import io.goorm.board.dto.category.CategoryUpdateDto;
import io.goorm.board.exception.category.CategoryNotFoundException;
import io.goorm.board.mapper.CategoryMapper;
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
public class CategoryServiceMyBatisImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ExcelExportService excelExportService;

    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto createDto) {
        log.debug("Creating category: {}", createDto.getName());

        // 정렬순서 설정 (기존 최대값 + 1)
        Integer maxSortOrder = categoryMapper.findMaxSortOrder();
        int nextSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 1;

        // DTO 생성
        CategoryDto categoryDto = CategoryDto.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .sortOrder(nextSortOrder)
                .isActive(true)
                .createdSeq(1L) // TODO: 현재 사용자 ID로 변경
                .updatedSeq(1L)
                .build();

        // 저장
        int result = categoryMapper.insert(categoryDto);
        if (result <= 0) {
            throw new RuntimeException("카테고리 등록에 실패했습니다.");
        }

        log.info("Category created successfully with seq: {}", categoryDto.getCategorySeq());
        return categoryDto;
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryUpdateDto updateDto) {
        log.debug("Updating category with seq: {}", updateDto.getCategorySeq());

        // 존재하는 카테고리인지 확인
        CategoryDto existingCategory = categoryMapper.findBySeq(updateDto.getCategorySeq());
        if (existingCategory == null) {
            throw new CategoryNotFoundException(updateDto.getCategorySeq());
        }

        // DTO 업데이트
        CategoryDto categoryDto = CategoryDto.builder()
                .categorySeq(updateDto.getCategorySeq())
                .name(updateDto.getName())
                .description(updateDto.getDescription())
                .updatedSeq(1L) // TODO: 현재 사용자 ID로 변경
                .build();

        // 저장
        int result = categoryMapper.update(categoryDto);
        if (result <= 0) {
            throw new RuntimeException("카테고리 수정에 실패했습니다.");
        }

        log.info("Category updated successfully with seq: {}", updateDto.getCategorySeq());

        // 수정된 카테고리 조회해서 반환
        return categoryMapper.findBySeq(updateDto.getCategorySeq());
    }

    @Override
    public CategoryDto findById(Long categorySeq) {
        CategoryDto category = categoryMapper.findBySeq(categorySeq);
        if (category == null) {
            throw new CategoryNotFoundException(categorySeq);
        }
        return category;
    }

    @Override
    public Page<CategoryDto> findAll(CategorySearchDto searchDto) {
        // 페이징 정보
        List<CategoryDto> categories = categoryMapper.findAll(searchDto);
        int total = categoryMapper.count(searchDto);

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, searchDto.getPage() - 1),
                searchDto.getSize()
        );

        return new PageImpl<>(categories, pageRequest, total);
    }

    @Override
    public List<CategoryDto> findAllForExport(CategorySearchDto searchDto) {
        log.debug("Finding all categories for export with search: {}", searchDto);

        // 페이징 무시하고 전체 데이터 조회
        searchDto.setPage(0);
        searchDto.setSize(Integer.MAX_VALUE);

        return categoryMapper.findAllForExcel(searchDto);
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
        return categoryMapper.findAllActive();
    }

    @Override
    public List<CategoryDto> findAllActiveOrSelected(Long selectedCategorySeq) {
        if (selectedCategorySeq == null) {
            return findAllActive();
        }
        return categoryMapper.findAllActiveOrSelected(selectedCategorySeq);
    }

    @Override
    @Transactional
    public void activate(Long categorySeq) {
        log.debug("Activating category with seq: {}", categorySeq);

        CategoryDto category = categoryMapper.findBySeq(categorySeq);
        if (category == null) {
            throw new CategoryNotFoundException(categorySeq);
        }

        int result = categoryMapper.activate(categorySeq);
        if (result <= 0) {
            throw new RuntimeException("카테고리 활성화에 실패했습니다.");
        }

        log.info("Category activated successfully with seq: {}", categorySeq);
    }

    @Override
    @Transactional
    public void deactivate(Long categorySeq) {
        log.debug("Deactivating category with seq: {}", categorySeq);

        CategoryDto category = categoryMapper.findBySeq(categorySeq);
        if (category == null) {
            throw new CategoryNotFoundException(categorySeq);
        }

        int result = categoryMapper.deactivate(categorySeq);
        if (result <= 0) {
            throw new RuntimeException("카테고리 비활성화에 실패했습니다.");
        }

        log.info("Category deactivated successfully with seq: {}", categorySeq);
    }

    @Override
    public CategoryDto findBySeq(Long categorySeq) {
        return findById(categorySeq);
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