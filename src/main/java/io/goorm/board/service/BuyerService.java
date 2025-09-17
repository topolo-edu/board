package io.goorm.board.service;

import io.goorm.board.dto.buyer.*;
import io.goorm.board.enums.BuyerStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BuyerService {

    List<BuyerDto> findAllActive();

    List<BuyerDto> findAllActiveOrSelected(Long selectedBuyerSeq);

    Page<BuyerDto> searchBuyers(BuyerSearchDto searchDto);

    BuyerDto findBySeq(Long buyerSeq);

    Long create(BuyerCreateDto createDto);

    void update(Long buyerSeq, BuyerUpdateDto updateDto);

    void activate(Long buyerSeq);

    void deactivate(Long buyerSeq);

    List<BuyerStatus> getBuyerStatuses();

    List<BuyerExcelDto> findBuyersForExcel(BuyerSearchDto searchDto);
}