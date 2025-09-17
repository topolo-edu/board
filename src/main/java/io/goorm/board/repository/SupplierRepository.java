package io.goorm.board.repository;

import io.goorm.board.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /**
     * 활성 공급업체 목록 조회 (정렬순서, 이름순)
     */
    List<Supplier> findByIsActiveOrderByNameAsc(Boolean isActive);

    /**
     * 수정용 공급업체 목록 조회 (활성 + 현재 선택된 공급업체)
     */
    @Query("SELECT s FROM Supplier s WHERE s.isActive = true OR s.supplierSeq = :selectedSupplierSeq ORDER BY s.name ASC")
    List<Supplier> findAllActiveOrSelected(@Param("selectedSupplierSeq") Long selectedSupplierSeq);

    /**
     * 공급업체 검색 (페이징)
     */
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.name LIKE %:keyword% OR s.contactPerson LIKE %:keyword%) AND " +
           "(:email IS NULL OR :email = '' OR s.email LIKE %:email%) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) " +
           "ORDER BY s.createdAt DESC")
    Page<Supplier> findSuppliersWithSearch(
            @Param("keyword") String keyword,
            @Param("email") String email,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * 공급업체 검색 (전체 - 엑셀용)
     */
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR s.name LIKE %:keyword% OR s.contactPerson LIKE %:keyword%) AND " +
           "(:email IS NULL OR :email = '' OR s.email LIKE %:email%) AND " +
           "(:isActive IS NULL OR s.isActive = :isActive) " +
           "ORDER BY s.createdAt DESC")
    List<Supplier> findSuppliersForExcel(
            @Param("keyword") String keyword,
            @Param("email") String email,
            @Param("isActive") Boolean isActive);

    /**
     * 이메일 중복 체크 (자신 제외)
     */
    boolean existsByEmailAndSupplierSeqNot(String email, Long supplierSeq);

    /**
     * 이메일 중복 체크 (신규)
     */
    boolean existsByEmail(String email);
}