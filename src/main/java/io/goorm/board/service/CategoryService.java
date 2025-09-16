package io.goorm.board.service;

import io.goorm.board.dto.CategoryDto;

import java.util.List;

/**
 * 카테고리 서비스 인터페이스
 */
public interface CategoryService {

    /**
     * 활성 카테고리 목록 조회
     */
    List<CategoryDto> findAllActive();

    /**
     * 수정용 카테고리 목록 조회 (활성 + 현재 선택된 카테고리)
     */
    List<CategoryDto> findAllActiveOrSelected(Long selectedCategorySeq);

    /**
     * 카테고리 상세 조회
     */
    CategoryDto findBySeq(Long categorySeq);
}