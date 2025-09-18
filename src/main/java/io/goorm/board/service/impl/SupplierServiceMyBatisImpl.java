package io.goorm.board.service.impl;

import io.goorm.board.dto.supplier.SupplierCreateDto;
import io.goorm.board.dto.supplier.SupplierDto;
import io.goorm.board.dto.supplier.SupplierExcelDto;
import io.goorm.board.dto.supplier.SupplierSearchDto;
import io.goorm.board.dto.supplier.SupplierUpdateDto;
import io.goorm.board.enums.SupplierStatus;
import io.goorm.board.exception.supplier.SupplierNotFoundException;
import io.goorm.board.mapper.SupplierMapper;
import io.goorm.board.service.ExcelExportService;
import io.goorm.board.service.SupplierService;
import io.goorm.board.util.ExcelUtil.CellType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 공급업체 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceMyBatisImpl implements SupplierService {

    private final SupplierMapper supplierMapper;
    private final ExcelExportService excelExportService;

    @Override
    @Transactional
    public Long create(SupplierCreateDto createDto) {
        log.debug("Creating supplier: {}", createDto.getName());

        // DTO 생성
        SupplierDto supplierDto = SupplierDto.builder()
                .name(createDto.getName())
                .contactPerson(createDto.getContactPerson())
                .email(createDto.getEmail())
                .phone(createDto.getPhone())
                .address(createDto.getAddress())
                .description(createDto.getDescription())
                .isActive(true)
                .createdSeq(1L) // TODO: 현재 사용자 ID로 변경
                .updatedSeq(1L)
                .build();

        // 저장
        int result = supplierMapper.insert(supplierDto);
        if (result <= 0) {
            throw new RuntimeException("공급업체 등록에 실패했습니다.");
        }

        log.info("Supplier created successfully with seq: {}", supplierDto.getSupplierSeq());
        return supplierDto.getSupplierSeq();
    }

    @Override
    @Transactional
    public void update(Long supplierSeq, SupplierUpdateDto updateDto) {
        log.debug("Updating supplier with seq: {}", supplierSeq);

        // 존재하는 공급업체인지 확인
        SupplierDto existingSupplier = supplierMapper.findBySeq(supplierSeq);
        if (existingSupplier == null) {
            throw new SupplierNotFoundException(supplierSeq);
        }

        // DTO 업데이트
        SupplierDto supplierDto = SupplierDto.builder()
                .supplierSeq(supplierSeq)
                .name(updateDto.getName())
                .contactPerson(updateDto.getContactPerson())
                .email(updateDto.getEmail())
                .phone(updateDto.getPhone())
                .address(updateDto.getAddress())
                .description(updateDto.getDescription())
                .updatedSeq(1L) // TODO: 현재 사용자 ID로 변경
                .build();

        // 저장
        int result = supplierMapper.update(supplierDto);
        if (result <= 0) {
            throw new RuntimeException("공급업체 수정에 실패했습니다.");
        }

        log.info("Supplier updated successfully with seq: {}", supplierSeq);
    }

    @Override
    public SupplierDto findBySeq(Long supplierSeq) {
        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new SupplierNotFoundException(supplierSeq);
        }
        return supplier;
    }

    @Override
    public Page<SupplierDto> searchSuppliers(SupplierSearchDto searchDto) {
        // 페이징 정보
        List<SupplierDto> suppliers = supplierMapper.findAll(searchDto);
        int total = supplierMapper.count(searchDto);

        PageRequest pageRequest = PageRequest.of(
                Math.max(0, searchDto.getPage() - 1),
                searchDto.getSize()
        );

        return new PageImpl<>(suppliers, pageRequest, total);
    }

    @Override
    public List<SupplierDto> findAllForExport(SupplierSearchDto searchDto) {
        log.debug("Finding all suppliers for export with search: {}", searchDto);

        // 페이징 무시하고 전체 데이터 조회
        searchDto.setPage(0);
        searchDto.setSize(Integer.MAX_VALUE);

        return supplierMapper.findAllForExcel(searchDto);
    }

    @Override
    public byte[] exportToExcel(SupplierSearchDto searchDto) {
        log.debug("Exporting suppliers to Excel with search: {}", searchDto);

        // Excel 내보내기용 공급업체 목록 조회
        List<SupplierDto> suppliers = findAllForExport(searchDto);

        // SupplierDto를 SupplierExcelDto로 변환
        List<SupplierExcelDto> excelSuppliers = suppliers.stream()
                .map(this::convertToExcelDto)
                .toList();

        // Excel 생성
        String[] headers = {
            "업체명", "담당자명", "이메일", "전화번호", "주소", "설명", "상태", "등록일", "수정일"
        };

        // 컬럼 타입 정의
        CellType[] columnTypes = {
            CellType.STRING,  // 업체명
            CellType.STRING,  // 담당자명
            CellType.STRING,  // 이메일
            CellType.STRING,  // 전화번호
            CellType.STRING,  // 주소
            CellType.STRING,  // 설명
            CellType.STRING,  // 상태
            CellType.STRING,  // 등록일
            CellType.STRING   // 수정일
        };

        return excelExportService.exportToExcelWithTypes("공급업체목록", headers, excelSuppliers,
                this::mapToRowDataWithTypes, columnTypes, LocaleContextHolder.getLocale());
    }

    @Override
    public List<SupplierDto> findAllActive() {
        return supplierMapper.findAllActive();
    }

    @Override
    public List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq) {
        if (selectedSupplierSeq == null) {
            return findAllActive();
        }
        return supplierMapper.findAllActiveOrSelected(selectedSupplierSeq);
    }

    @Override
    @Transactional
    public void activate(Long supplierSeq) {
        log.debug("Activating supplier with seq: {}", supplierSeq);

        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new SupplierNotFoundException(supplierSeq);
        }

        int result = supplierMapper.activate(supplierSeq);
        if (result <= 0) {
            throw new RuntimeException("공급업체 활성화에 실패했습니다.");
        }

        log.info("Supplier activated successfully with seq: {}", supplierSeq);
    }

    @Override
    @Transactional
    public void deactivate(Long supplierSeq) {
        log.debug("Deactivating supplier with seq: {}", supplierSeq);

        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new SupplierNotFoundException(supplierSeq);
        }

        int result = supplierMapper.deactivate(supplierSeq);
        if (result <= 0) {
            throw new RuntimeException("공급업체 비활성화에 실패했습니다.");
        }

        log.info("Supplier deactivated successfully with seq: {}", supplierSeq);
    }

    @Override
    public List<SupplierStatus> getSupplierStatuses() {
        return Arrays.asList(SupplierStatus.values());
    }

    @Override
    public List<SupplierExcelDto> findSuppliersForExcel(SupplierSearchDto searchDto) {
        List<SupplierDto> suppliers = findAllForExport(searchDto);
        return suppliers.stream().map(this::convertToExcelDto).toList();
    }

    /**
     * SupplierDto를 SupplierExcelDto로 변환
     */
    private SupplierExcelDto convertToExcelDto(SupplierDto supplier) {
        return SupplierExcelDto.builder()
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .description(supplier.getDescription())
                .status(supplier.getStatusDisplayName())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }

    /**
     * SupplierExcelDto를 Excel 행 데이터로 변환 (타입별 Object 배열)
     */
    private Object[] mapToRowDataWithTypes(SupplierExcelDto supplier) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return new Object[] {
            supplier.getName(),                         // STRING
            supplier.getContactPerson(),                // STRING
            supplier.getEmail(),                        // STRING
            supplier.getPhone(),                        // STRING
            supplier.getAddress(),                      // STRING
            supplier.getDescription(),                  // STRING
            supplier.getStatus(),                       // STRING
            supplier.getCreatedAt() != null ? supplier.getCreatedAt().format(formatter) : "", // STRING
            supplier.getUpdatedAt() != null ? supplier.getUpdatedAt().format(formatter) : ""  // STRING
        };
    }
}