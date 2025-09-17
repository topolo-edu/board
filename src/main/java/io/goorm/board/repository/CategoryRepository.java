package io.goorm.board.repository;

import io.goorm.board.entity.Category;
import io.goorm.board.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    /**
     * 활성 카테고리 전체 조회
     */
    List<Category> findByIsActiveOrderBySortOrderAsc(Boolean isActive);

    /**
     * 검색 조건에 따른 카테고리 조회 (페이징)
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR c.name LIKE %:keyword%) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) " +
           "ORDER BY c.sortOrder ASC, c.createdAt DESC")
    Page<Category> findBySearchCondition(@Param("keyword") String keyword,
                                       @Param("isActive") Boolean isActive,
                                       Pageable pageable);

    /**
     * 검색 조건에 따른 카테고리 전체 조회 (Excel 내보내기용)
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR c.name LIKE %:keyword%) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive) " +
           "ORDER BY c.sortOrder ASC, c.createdAt DESC")
    List<Category> findAllBySearchCondition(@Param("keyword") String keyword,
                                          @Param("isActive") Boolean isActive);

    /**
     * 정렬순서 최대값 조회
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c")
    Integer findMaxSortOrder();
}