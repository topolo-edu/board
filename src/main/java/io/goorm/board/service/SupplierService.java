package io.goorm.board.service;

import io.goorm.board.dto.supplier.*;
import io.goorm.board.enums.SupplierStatus;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 공급업체 서비스 인터페이스
 */
public interface SupplierService {

    /**
     * 공급업체 등록
     */
    Long create(SupplierCreateDto createDto);

    /**
     * 공급업체 수정
     */
    void update(Long supplierSeq, SupplierUpdateDto updateDto);

    /**
     * 공급업체 단건 조회
     */
    SupplierDto findBySeq(Long supplierSeq);

    /**
     * 공급업체 목록 조회 (페이징)
     */
    Page<SupplierDto> searchSuppliers(SupplierSearchDto searchDto);

    /**
     * Excel 내보내기용 공급업체 목록 조회 (페이징 없음)
     */
    List<SupplierDto> findAllForExport(SupplierSearchDto searchDto);

    /**
     * 공급업체 목록 Excel 내보내기
     */
    byte[] exportToExcel(SupplierSearchDto searchDto);

    /**
     * 활성 공급업체 목록 조회
     */
    List<SupplierDto> findAllActive();

    /**
     * 활성 공급업체 목록 조회 (선택된 공급업체 포함)
     */
    List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq);

    /**
     * 공급업체 활성화
     */
    void activate(Long supplierSeq);

    /**
     * 공급업체 비활성화
     */
    void deactivate(Long supplierSeq);

    /**
     * 공급업체 상태 목록 조회
     */
    List<SupplierStatus> getSupplierStatuses();

    /**
     * 엑셀 다운로드용 데이터 조회 (기존 호환성)
     */
    List<SupplierExcelDto> findSuppliersForExcel(SupplierSearchDto searchDto);
}