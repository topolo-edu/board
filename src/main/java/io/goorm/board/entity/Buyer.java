package io.goorm.board.entity;

import io.goorm.board.enums.BuyerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buyers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Buyer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "buyer_seq")
    private Long buyerSeq;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "total_order_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalOrderAmount = BigDecimal.ZERO;

    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;

    @Column(name = "payment_terms", length = 50)
    @Builder.Default
    private String paymentTerms = "월말결제";

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_seq")
    private Long createdSeq;

    @Column(name = "updated_seq")
    private Long updatedSeq;

    public BuyerStatus getStatus() {
        return isActive ? BuyerStatus.ACTIVE : BuyerStatus.INACTIVE;
    }

    public void updateBasicInfo(String companyName, String businessNumber, String contactPerson,
                               String email, String phone, String address, String description) {
        this.companyName = companyName;
        this.businessNumber = businessNumber;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.description = description;
    }

    public void updateCreditInfo(BigDecimal creditLimit, BigDecimal discountRate, String paymentTerms) {
        this.creditLimit = creditLimit;
        this.discountRate = discountRate;
        this.paymentTerms = paymentTerms;
    }

    public void updateOrderInfo(BigDecimal orderAmount) {
        this.totalOrderAmount = this.totalOrderAmount.add(orderAmount);
        this.lastOrderDate = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}