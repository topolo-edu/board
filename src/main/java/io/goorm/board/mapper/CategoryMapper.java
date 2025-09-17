package io.goorm.board.mapper;

import io.goorm.board.dto.category.CategoryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 카테고리 매퍼
 */
@Mapper
public interface CategoryMapper {

    /**
     * 활성 카테고리 목록 조회 (정렬 순서대로)
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