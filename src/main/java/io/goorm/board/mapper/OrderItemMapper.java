package io.goorm.board.mapper;

import io.goorm.board.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 발주 상품 관련 MyBatis Mapper 인터페이스
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 발주 상품 등록
     */
    int insert(OrderItem orderItem);

    /**
     * 발주 상품 일괄 등록
     */
    int insertBatch(@Param("orderItems") List<OrderItem> orderItems);

    /**
     * 발주 상품 수정
     */
    int update(OrderItem orderItem);

    /**
     * 발주 상품 삭제
     */
    int deleteById(Long orderItemSeq);

    /**
     * 발주별 상품 목록 조회
     */
    List<OrderItem> findByOrderSeq(Long orderSeq);

    /**
     * 발주별 상품 삭제
     */
    int deleteByOrderSeq(Long orderSeq);
}