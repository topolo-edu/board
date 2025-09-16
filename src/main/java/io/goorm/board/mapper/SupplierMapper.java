package io.goorm.board.mapper;

import io.goorm.board.dto.SupplierDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 공급업체 매퍼
 */
@Mapper
public interface SupplierMapper {

    /**
     * 활성 공급업체 목록 조회 (이름순 정렬)
     */
    List<SupplierDto> findAllActive();

    /**
     * 수정용 공급업체 목록 조회 (활성 + 현재 선택된 공급업체)
     */
    List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq);

    /**
     * 공급업체 상세 조회
     */
    SupplierDto findBySeq(Long supplierSeq);
}