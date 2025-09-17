package io.goorm.board.service;

import io.goorm.board.dto.category.CategoryCreateDto;
import io.goorm.board.dto.category.CategoryDto;
import io.goorm.board.dto.category.CategorySearchDto;
import io.goorm.board.dto.category.CategoryUpdateDto;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 카테고리 서비스 인터페이스
 */
public interface CategoryService {

    /**
     * 카테고리 등록
     */
    CategoryDto create(CategoryCreateDto createDto);

    /**
     * 카테고리 수정
     */
    CategoryDto update(CategoryUpdateDto updateDto);

    /**
     * 카테고리 단건 조회
     */
    CategoryDto findById(Long categorySeq);

    /**
     * 카테고리 목록 조회 (페이징)
     */
    Page<CategoryDto> findAll(CategorySearchDto searchDto);

    /**
     * Excel 내보내기용 카테고리 목록 조회 (페이징 없음)
     */
    List<CategoryDto> findAllForExport(CategorySearchDto searchDto);

    /**
     * 카테고리 목록 Excel 내보내기
     */
    byte[] exportToExcel(CategorySearchDto searchDto);

    /**
     * 활성 카테고리 목록 조회
     */
    List<CategoryDto> findAllActive();

    /**
     * 활성 카테고리 목록 조회 (선택된 카테고리 포함)
     */
    List<CategoryDto> findAllActiveOrSelected(Long selectedCategorySeq);

    /**
     * 카테고리 활성화
     */
    void activate(Long categorySeq);

    /**
     * 카테고리 비활성화
     */
    void deactivate(Long categorySeq);

    /**
     * 카테고리 상세 조회 (기존 호환성)
     */
    CategoryDto findBySeq(Long categorySeq);
}