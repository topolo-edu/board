package io.goorm.board.mapper;

import io.goorm.board.dto.supplier.SupplierDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierMapper {

    /**
     * 활성 공급업체 목록 조회 (이름순 정렬)
     */
    List<SupplierDto> findAllActive();

    /**
     * 활성 공급업체 + 선택된 공급업체 조회 (수정용)
     */
    List<SupplierDto> findAllActiveOrSelected(@Param("selectedSupplierSeq") Long selectedSupplierSeq);

}