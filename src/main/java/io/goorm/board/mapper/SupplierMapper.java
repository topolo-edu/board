package io.goorm.board.mapper;

import io.goorm.board.dto.supplier.SupplierDto;
import io.goorm.board.dto.supplier.SupplierSearchDto;
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

    /**
     * 공급업체 검색 (페이징)
     */
    List<SupplierDto> findAll(@Param("search") SupplierSearchDto searchDto);

    /**
     * 공급업체 검색 개수
     */
    int count(@Param("search") SupplierSearchDto searchDto);

    /**
     * 공급업체 상세 조회
     */
    SupplierDto findBySeq(@Param("supplierSeq") Long supplierSeq);

    /**
     * 공급업체 등록
     */
    int insert(@Param("supplier") SupplierDto supplierDto);

    /**
     * 공급업체 수정
     */
    int update(@Param("supplier") SupplierDto supplierDto);

    /**
     * 공급업체 활성화
     */
    int activate(@Param("supplierSeq") Long supplierSeq);

    /**
     * 공급업체 비활성화
     */
    int deactivate(@Param("supplierSeq") Long supplierSeq);

    /**
     * 이메일 중복 체크
     */
    int countByEmail(@Param("email") String email);

    /**
     * 이메일 중복 체크 (자신 제외)
     */
    int countByEmailAndNotSeq(@Param("email") String email, @Param("supplierSeq") Long supplierSeq);

    /**
     * Excel용 전체 공급업체 조회
     */
    List<SupplierDto> findAllForExcel(@Param("search") SupplierSearchDto searchDto);
}