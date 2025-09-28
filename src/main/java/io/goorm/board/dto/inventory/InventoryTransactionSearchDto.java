package io.goorm.board.dto.inventory;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 재고 거래 내역 검색 DTO
 */
@Data
public class InventoryTransactionSearchDto {

    private String transactionType;  // 거래 타입 (IN, OUT)
    private Long productSeq;         // 상품 번호
    private String productCode;      // 상품 코드
    private String productName;      // 상품명
    private Long supplierSeq;        // 공급업체 번호
    private String startDate;        // 시작일
    private String endDate;          // 종료일
    private String createdBy;        // 생성자

    // 페이징 관련
    private int page = 1;            // 현재 페이지 (기본값: 1)
    private int pageSize = 20;       // 페이지 크기 (기본값: 20)
    private int offset;              // OFFSET 값

    // 정렬 관련
    private String orderBy = "created_at";  // 정렬 기준 (기본값: 생성일)
    private String orderDirection = "DESC"; // 정렬 방향 (기본값: 내림차순)

    /**
     * OFFSET 계산
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /**
     * 유효한 페이지 번호인지 확인
     */
    public void validatePage() {
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
    }
}