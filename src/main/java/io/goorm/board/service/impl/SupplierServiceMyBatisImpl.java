package io.goorm.board.service.impl;

import io.goorm.board.dto.category.*;
import io.goorm.board.dto.supplier.SupplierDto;
import io.goorm.board.mapper.SupplierMapper;
import io.goorm.board.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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


}