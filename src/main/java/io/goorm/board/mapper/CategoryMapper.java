package io.goorm.board.mapper;

import io.goorm.board.dto.category.CategoryDto;
import io.goorm.board.dto.category.CategorySearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * 활성 카테고리 + 선택된 카테고리 조회 (수정용)
     */
    List<CategoryDto> findAllActiveOrSelected(@Param("selectedCategorySeq") Long selectedCategorySeq);

    /**
     * 카테고리 검색 (페이징)
     */
    List<CategoryDto> findAll(@Param("search") CategorySearchDto searchDto);

    /**
     * 카테고리 검색 개수
     */
    int count(@Param("search") CategorySearchDto searchDto);

    /**
     * 카테고리 상세 조회
     */
    CategoryDto findBySeq(@Param("categorySeq") Long categorySeq);

    /**
     * 카테고리 등록
     */
    int insert(@Param("category") CategoryDto categoryDto);

    /**
     * 카테고리 수정
     */
    int update(@Param("category") CategoryDto categoryDto);

    /**
     * 카테고리 활성화
     */
    int activate(@Param("categorySeq") Long categorySeq);

    /**
     * 카테고리 비활성화
     */
    int deactivate(@Param("categorySeq") Long categorySeq);

    /**
     * 최대 정렬 순서 조회
     */
    Integer findMaxSortOrder();

    /**
     * Excel용 전체 카테고리 조회
     */
    List<CategoryDto> findAllForExcel(@Param("search") CategorySearchDto searchDto);
}