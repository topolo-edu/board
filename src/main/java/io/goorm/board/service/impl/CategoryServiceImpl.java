package io.goorm.board.service.impl;

import io.goorm.board.dto.CategoryDto;
import io.goorm.board.mapper.CategoryMapper;
import io.goorm.board.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 카테고리 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> findAllActive() {
        log.debug("활성 카테고리 목록 조회");
        return categoryMapper.findAllActive();
    }

    @Override
    public List<CategoryDto> findAllActiveOrSelected(Long selectedCategorySeq) {
        log.debug("수정용 카테고리 목록 조회 (활성 + 선택된 카테고리: {})", selectedCategorySeq);
        return categoryMapper.findAllActiveOrSelected(selectedCategorySeq);
    }

    @Override
    public CategoryDto findBySeq(Long categorySeq) {
        log.debug("카테고리 상세 조회: {}", categorySeq);
        return categoryMapper.findBySeq(categorySeq);
    }
}