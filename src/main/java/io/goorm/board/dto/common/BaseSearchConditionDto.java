package io.goorm.board.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 검색 조건의 공통 기반 클래스
 * 모든 검색 DTO가 상속받아 사용하는 공통 필드와 메서드를 정의
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseSearchConditionDto {
    
    // ========== 공통 검색 필드 ==========
    protected String keyword;                // 검색 키워드
    @Builder.Default
    protected String searchType = "all";     // 검색 타입
    
    // ========== 공통 페이징 필드 ==========
    @Builder.Default
    protected int page = 1;                  // 페이지 번호
    @Builder.Default
    protected int size = 10;                 // 페이지 크기
    
    // ========== 공통 정렬 필드 ==========
    @Builder.Default
    protected String sortBy = "created_at";  // 정렬 기준
    @Builder.Default
    protected String sortDirection = "DESC"; // 정렬 방향
    
    // ========== 공통 유틸리티 메서드 ==========
    
    /**
     * 페이징을 위한 오프셋 계산
     */
    public int getOffset() {
        return Math.max(0, (page - 1) * size);
    }
    
    /**
     * 키워드 검색 여부 확인
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 기본 정렬 여부 확인
     */
    public boolean isDefaultSort() {
        return "created_at".equals(sortBy) && "DESC".equals(sortDirection);
    }
    
    /**
     * 각 하위 클래스에서 구현해야 하는 비어있음 체크
     * 각 도메인별로 고유한 검색 조건들을 확인
     */
    public abstract boolean isEmpty();
}
