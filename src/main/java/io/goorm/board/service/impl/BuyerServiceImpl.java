package io.goorm.board.service.impl;

import io.goorm.board.dto.buyer.*;
import io.goorm.board.entity.Buyer;
import io.goorm.board.enums.BuyerStatus;
import io.goorm.board.repository.BuyerRepository;
import io.goorm.board.service.BuyerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;

    @Override
    public List<BuyerDto> findAllActive() {
        List<Buyer> buyers = buyerRepository.findByIsActiveOrderByCompanyNameAsc(true);
        return buyers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuyerDto> findAllActiveOrSelected(Long selectedBuyerSeq) {
        if (selectedBuyerSeq == null) {
            return findAllActive();
        }
        List<Buyer> buyers = buyerRepository.findAllActiveOrSelected(selectedBuyerSeq);
        return buyers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BuyerDto> searchBuyers(BuyerSearchDto searchDto) {
        Pageable pageable = PageRequest.of(searchDto.getPage() - 1, searchDto.getSize());

        String keyword = StringUtils.hasText(searchDto.getKeyword()) ? searchDto.getKeyword() : null;
        String email = StringUtils.hasText(searchDto.getEmail()) ? searchDto.getEmail() : null;
        String businessNumber = StringUtils.hasText(searchDto.getBusinessNumber()) ? searchDto.getBusinessNumber() : null;
        Boolean isActive = searchDto.getStatus() != null ?
            (searchDto.getStatus() == BuyerStatus.ACTIVE) : null;

        Page<Buyer> buyers = buyerRepository.findBuyersWithSearch(
                keyword, email, businessNumber, isActive, pageable);

        return buyers.map(this::convertToDto);
    }

    @Override
    public BuyerDto findBySeq(Long buyerSeq) {
        Buyer buyer = buyerRepository.findById(buyerSeq)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다. ID: " + buyerSeq));
        return convertToDto(buyer);
    }

    @Override
    @Transactional
    public Long create(BuyerCreateDto createDto) {
        if (StringUtils.hasText(createDto.getEmail()) &&
            buyerRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (StringUtils.hasText(createDto.getBusinessNumber()) &&
            buyerRepository.existsByBusinessNumber(createDto.getBusinessNumber())) {
            throw new RuntimeException("이미 사용 중인 사업자등록번호입니다.");
        }

        Buyer buyer = Buyer.builder()
                .companyName(createDto.getCompanyName())
                .businessNumber(createDto.getBusinessNumber())
                .contactPerson(createDto.getContactPerson())
                .email(createDto.getEmail())
                .phone(createDto.getPhone())
                .address(createDto.getAddress())
                .creditLimit(createDto.getCreditLimit() != null ? createDto.getCreditLimit() : BigDecimal.ZERO)
                .discountRate(createDto.getDiscountRate() != null ? createDto.getDiscountRate() : BigDecimal.ZERO)
                .paymentTerms(createDto.getPaymentTerms() != null ? createDto.getPaymentTerms() : "월말결제")
                .description(createDto.getDescription())
                .isActive(true)
                .build();

        Buyer savedBuyer = buyerRepository.save(buyer);
        log.info("고객이 등록되었습니다. ID: {}, Company: {}", savedBuyer.getBuyerSeq(), savedBuyer.getCompanyName());

        return savedBuyer.getBuyerSeq();
    }

    @Override
    @Transactional
    public void update(Long buyerSeq, BuyerUpdateDto updateDto) {
        Buyer buyer = buyerRepository.findById(buyerSeq)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다. ID: " + buyerSeq));

        if (StringUtils.hasText(updateDto.getEmail()) &&
            buyerRepository.existsByEmailAndBuyerSeqNot(updateDto.getEmail(), buyerSeq)) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if (StringUtils.hasText(updateDto.getBusinessNumber()) &&
            buyerRepository.existsByBusinessNumberAndBuyerSeqNot(updateDto.getBusinessNumber(), buyerSeq)) {
            throw new RuntimeException("이미 사용 중인 사업자등록번호입니다.");
        }

        buyer.updateBasicInfo(
                updateDto.getCompanyName(),
                updateDto.getBusinessNumber(),
                updateDto.getContactPerson(),
                updateDto.getEmail(),
                updateDto.getPhone(),
                updateDto.getAddress(),
                updateDto.getDescription()
        );

        buyer.updateCreditInfo(
                updateDto.getCreditLimit() != null ? updateDto.getCreditLimit() : BigDecimal.ZERO,
                updateDto.getDiscountRate() != null ? updateDto.getDiscountRate() : BigDecimal.ZERO,
                updateDto.getPaymentTerms() != null ? updateDto.getPaymentTerms() : "월말결제"
        );

        log.info("고객이 수정되었습니다. ID: {}, Company: {}", buyerSeq, buyer.getCompanyName());
    }

    @Override
    @Transactional
    public void activate(Long buyerSeq) {
        Buyer buyer = buyerRepository.findById(buyerSeq)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다. ID: " + buyerSeq));

        buyer.activate();
        log.info("고객이 활성화되었습니다. ID: {}, Company: {}", buyerSeq, buyer.getCompanyName());
    }

    @Override
    @Transactional
    public void deactivate(Long buyerSeq) {
        Buyer buyer = buyerRepository.findById(buyerSeq)
                .orElseThrow(() -> new RuntimeException("고객을 찾을 수 없습니다. ID: " + buyerSeq));

        buyer.deactivate();
        log.info("고객이 비활성화되었습니다. ID: {}, Company: {}", buyerSeq, buyer.getCompanyName());
    }

    @Override
    public List<BuyerStatus> getBuyerStatuses() {
        return Arrays.asList(BuyerStatus.values());
    }

    @Override
    public List<BuyerExcelDto> findBuyersForExcel(BuyerSearchDto searchDto) {
        String keyword = StringUtils.hasText(searchDto.getKeyword()) ? searchDto.getKeyword() : null;
        String email = StringUtils.hasText(searchDto.getEmail()) ? searchDto.getEmail() : null;
        String businessNumber = StringUtils.hasText(searchDto.getBusinessNumber()) ? searchDto.getBusinessNumber() : null;
        Boolean isActive = searchDto.getStatus() != null ?
            (searchDto.getStatus() == BuyerStatus.ACTIVE) : null;

        List<Buyer> buyers = buyerRepository.findBuyersForExcel(keyword, email, businessNumber, isActive);

        return buyers.stream()
                .map(this::convertToExcelDto)
                .collect(Collectors.toList());
    }

    private BuyerDto convertToDto(Buyer buyer) {
        return BuyerDto.builder()
                .buyerSeq(buyer.getBuyerSeq())
                .companyName(buyer.getCompanyName())
                .businessNumber(buyer.getBusinessNumber())
                .contactPerson(buyer.getContactPerson())
                .email(buyer.getEmail())
                .phone(buyer.getPhone())
                .address(buyer.getAddress())
                .creditLimit(buyer.getCreditLimit())
                .discountRate(buyer.getDiscountRate())
                .totalOrderAmount(buyer.getTotalOrderAmount())
                .lastOrderDate(buyer.getLastOrderDate())
                .paymentTerms(buyer.getPaymentTerms())
                .description(buyer.getDescription())
                .status(buyer.getStatus())
                .createdAt(buyer.getCreatedAt())
                .updatedAt(buyer.getUpdatedAt())
                .createdSeq(buyer.getCreatedSeq())
                .updatedSeq(buyer.getUpdatedSeq())
                .build();
    }

    private BuyerExcelDto convertToExcelDto(Buyer buyer) {
        return BuyerExcelDto.builder()
                .companyName(buyer.getCompanyName())
                .businessNumber(buyer.getBusinessNumber())
                .contactPerson(buyer.getContactPerson())
                .email(buyer.getEmail())
                .phone(buyer.getPhone())
                .address(buyer.getAddress())
                .creditLimit(buyer.getCreditLimit())
                .discountRate(buyer.getDiscountRate())
                .totalOrderAmount(buyer.getTotalOrderAmount())
                .lastOrderDate(buyer.getLastOrderDate())
                .paymentTerms(buyer.getPaymentTerms())
                .description(buyer.getDescription())
                .status(buyer.getStatus().getDisplayName())
                .createdAt(buyer.getCreatedAt())
                .updatedAt(buyer.getUpdatedAt())
                .build();
    }
}