package io.goorm.board.repository;

import io.goorm.board.entity.Buyer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    List<Buyer> findByIsActiveOrderByCompanyNameAsc(Boolean isActive);

    @Query("SELECT b FROM Buyer b WHERE b.isActive = true OR b.buyerSeq = :selectedBuyerSeq ORDER BY b.companyName ASC")
    List<Buyer> findAllActiveOrSelected(@Param("selectedBuyerSeq") Long selectedBuyerSeq);

    @Query("SELECT b FROM Buyer b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR b.companyName LIKE %:keyword% OR b.contactPerson LIKE %:keyword%) AND " +
           "(:email IS NULL OR :email = '' OR b.email LIKE %:email%) AND " +
           "(:businessNumber IS NULL OR :businessNumber = '' OR b.businessNumber LIKE %:businessNumber%) AND " +
           "(:isActive IS NULL OR b.isActive = :isActive) " +
           "ORDER BY b.createdAt DESC")
    Page<Buyer> findBuyersWithSearch(
            @Param("keyword") String keyword,
            @Param("email") String email,
            @Param("businessNumber") String businessNumber,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT b FROM Buyer b WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR b.companyName LIKE %:keyword% OR b.contactPerson LIKE %:keyword%) AND " +
           "(:email IS NULL OR :email = '' OR b.email LIKE %:email%) AND " +
           "(:businessNumber IS NULL OR :businessNumber = '' OR b.businessNumber LIKE %:businessNumber%) AND " +
           "(:isActive IS NULL OR b.isActive = :isActive) " +
           "ORDER BY b.createdAt DESC")
    List<Buyer> findBuyersForExcel(
            @Param("keyword") String keyword,
            @Param("email") String email,
            @Param("businessNumber") String businessNumber,
            @Param("isActive") Boolean isActive);

    boolean existsByEmailAndBuyerSeqNot(String email, Long buyerSeq);

    boolean existsByEmail(String email);

    boolean existsByBusinessNumberAndBuyerSeqNot(String businessNumber, Long buyerSeq);

    boolean existsByBusinessNumber(String businessNumber);
}