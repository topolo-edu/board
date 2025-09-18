package io.goorm.board.service.impl;

import io.goorm.board.dto.supplier.*;
import io.goorm.board.enums.SupplierStatus;
import io.goorm.board.mapper.SupplierMapper;
import io.goorm.board.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SupplierServiceMyBatisImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

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
    public SupplierDto findBySeq(Long supplierSeq) {
        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq);
        }
        return supplier;
    }

    @Override
    @Transactional
    public Long create(SupplierCreateDto createDto) {
        // 이메일 중복 체크
        if (createDto.getEmail() != null && !createDto.getEmail().trim().isEmpty()) {
            int count = supplierMapper.countByEmail(createDto.getEmail());
            if (count > 0) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
        }

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

        int result = supplierMapper.insert(supplierDto);
        if (result <= 0) {
            throw new RuntimeException("공급업체 등록에 실패했습니다.");
        }

        log.info("공급업체가 등록되었습니다. ID: {}, Name: {}", supplierDto.getSupplierSeq(), supplierDto.getName());
        return supplierDto.getSupplierSeq();
    }

    @Override
    @Transactional
    public void update(Long supplierSeq, SupplierUpdateDto updateDto) {
        // 공급업체 존재 확인
        SupplierDto existingSupplier = supplierMapper.findBySeq(supplierSeq);
        if (existingSupplier == null) {
            throw new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq);
        }

        // 이메일 중복 체크 (자신 제외)
        if (updateDto.getEmail() != null && !updateDto.getEmail().trim().isEmpty()) {
            int count = supplierMapper.countByEmailAndNotSeq(updateDto.getEmail(), supplierSeq);
            if (count > 0) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
        }

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

        int result = supplierMapper.update(supplierDto);
        if (result <= 0) {
            throw new RuntimeException("공급업체 수정에 실패했습니다.");
        }

        log.info("공급업체가 수정되었습니다. ID: {}, Name: {}", supplierSeq, updateDto.getName());
    }

    @Override
    @Transactional
    public void activate(Long supplierSeq) {
        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq);
        }

        int result = supplierMapper.activate(supplierSeq);
        if (result <= 0) {
            throw new RuntimeException("공급업체 활성화에 실패했습니다.");
        }

        log.info("공급업체가 활성화되었습니다. ID: {}, Name: {}", supplierSeq, supplier.getName());
    }

    @Override
    @Transactional
    public void deactivate(Long supplierSeq) {
        SupplierDto supplier = supplierMapper.findBySeq(supplierSeq);
        if (supplier == null) {
            throw new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq);
        }

        int result = supplierMapper.deactivate(supplierSeq);
        if (result <= 0) {
            throw new RuntimeException("공급업체 비활성화에 실패했습니다.");
        }

        log.info("공급업체가 비활성화되었습니다. ID: {}, Name: {}", supplierSeq, supplier.getName());
    }

    @Override
    public List<SupplierStatus> getSupplierStatuses() {
        return Arrays.asList(SupplierStatus.values());
    }

    @Override
    public List<SupplierExcelDto> findSuppliersForExcel(SupplierSearchDto searchDto) {
        // 페이징 무시하고 전체 데이터 조회
        searchDto.setPage(0);
        searchDto.setSize(Integer.MAX_VALUE);

        List<SupplierDto> suppliers = supplierMapper.findAllForExcel(searchDto);

        return suppliers.stream()
                .map(this::convertToExcelDto)
                .collect(Collectors.toList());
    }

    private SupplierExcelDto convertToExcelDto(SupplierDto supplier) {
        return SupplierExcelDto.builder()
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .description(supplier.getDescription())
                .status(supplier.getStatus().getDisplayName())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}