package io.goorm.board.service;

import io.goorm.board.dto.supplier.*;

import java.util.List;

public interface SupplierService {

    /**
     * 활성 공급업체 목록 조회
     */
    List<SupplierDto> findAllActive();

    /**
     * 수정용 공급업체 목록 조회 (활성 + 현재 선택된 공급업체)
     */
    List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq);

}