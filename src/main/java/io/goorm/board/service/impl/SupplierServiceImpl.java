package io.goorm.board.service.impl;

import io.goorm.board.dto.supplier.*;
import io.goorm.board.entity.Supplier;
import io.goorm.board.enums.SupplierStatus;
import io.goorm.board.repository.SupplierRepository;
import io.goorm.board.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public List<SupplierDto> findAllActive() {
        List<Supplier> suppliers = supplierRepository.findByIsActiveOrderByNameAsc(true);
        return suppliers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDto> findAllActiveOrSelected(Long selectedSupplierSeq) {
        if (selectedSupplierSeq == null) {
            return findAllActive();
        }
        List<Supplier> suppliers = supplierRepository.findAllActiveOrSelected(selectedSupplierSeq);
        return suppliers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SupplierDto> searchSuppliers(SupplierSearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPage() - 1, searchDto.getSize());

        String keyword = StringUtils.hasText(searchDto.getKeyword()) ? searchDto.getKeyword() : null;
        String email = StringUtils.hasText(searchDto.getEmail()) ? searchDto.getEmail() : null;
        Boolean isActive = searchDto.getStatus() != null ?
            (searchDto.getStatus() == SupplierStatus.ACTIVE) : null;

        Page<Supplier> suppliers = supplierRepository.findSuppliersWithSearch(
                keyword, email, isActive, pageable);

        return suppliers.map(this::convertToDto);
    }

    @Override
    public SupplierDto findBySeq(Long supplierSeq) {
        Supplier supplier = supplierRepository.findById(supplierSeq)
                .orElseThrow(() -> new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq));
        return convertToDto(supplier);
    }

    @Override
    @Transactional
    public Long create(SupplierCreateDto createDto) {
        // 이메일 중복 체크
        if (StringUtils.hasText(createDto.getEmail()) &&
            supplierRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        Supplier supplier = Supplier.builder()
                .name(createDto.getName())
                .contactPerson(createDto.getContactPerson())
                .email(createDto.getEmail())
                .phone(createDto.getPhone())
                .address(createDto.getAddress())
                .description(createDto.getDescription())
                .isActive(true)
                .build();

        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("공급업체가 등록되었습니다. ID: {}, Name: {}", savedSupplier.getSupplierSeq(), savedSupplier.getName());

        return savedSupplier.getSupplierSeq();
    }

    @Override
    @Transactional
    public void update(Long supplierSeq, SupplierUpdateDto updateDto) {
        Supplier supplier = supplierRepository.findById(supplierSeq)
                .orElseThrow(() -> new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq));

        // 이메일 중복 체크 (자신 제외)
        if (StringUtils.hasText(updateDto.getEmail()) &&
            supplierRepository.existsByEmailAndSupplierSeqNot(updateDto.getEmail(), supplierSeq)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        supplier.updateBasicInfo(
                updateDto.getName(),
                updateDto.getContactPerson(),
                updateDto.getEmail(),
                updateDto.getPhone(),
                updateDto.getAddress(),
                updateDto.getDescription()
        );

        log.info("공급업체가 수정되었습니다. ID: {}, Name: {}", supplierSeq, supplier.getName());
    }

    @Override
    @Transactional
    public void activate(Long supplierSeq) {
        Supplier supplier = supplierRepository.findById(supplierSeq)
                .orElseThrow(() -> new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq));

        supplier.activate();
        log.info("공급업체가 활성화되었습니다. ID: {}, Name: {}", supplierSeq, supplier.getName());
    }

    @Override
    @Transactional
    public void deactivate(Long supplierSeq) {
        Supplier supplier = supplierRepository.findById(supplierSeq)
                .orElseThrow(() -> new RuntimeException("공급업체를 찾을 수 없습니다. ID: " + supplierSeq));

        supplier.deactivate();
        log.info("공급업체가 비활성화되었습니다. ID: {}, Name: {}", supplierSeq, supplier.getName());
    }

    @Override
    public List<SupplierStatus> getSupplierStatuses() {
        return Arrays.asList(SupplierStatus.values());
    }

    @Override
    public List<SupplierExcelDto> findSuppliersForExcel(SupplierSearchDto searchDto) {
        String keyword = StringUtils.hasText(searchDto.getKeyword()) ? searchDto.getKeyword() : null;
        String email = StringUtils.hasText(searchDto.getEmail()) ? searchDto.getEmail() : null;
        Boolean isActive = searchDto.getStatus() != null ?
            (searchDto.getStatus() == SupplierStatus.ACTIVE) : null;

        List<Supplier> suppliers = supplierRepository.findSuppliersForExcel(keyword, email, isActive);

        return suppliers.stream()
                .map(this::convertToExcelDto)
                .collect(Collectors.toList());
    }

    private SupplierDto convertToDto(Supplier supplier) {
        return SupplierDto.builder()
                .supplierSeq(supplier.getSupplierSeq())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .description(supplier.getDescription())
                .status(supplier.getStatus())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .createdSeq(supplier.getCreatedSeq())
                .updatedSeq(supplier.getUpdatedSeq())
                .build();
    }

    private SupplierExcelDto convertToExcelDto(Supplier supplier) {
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