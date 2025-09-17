package io.goorm.board.entity;

import io.goorm.board.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 카테고리 엔티티
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_seq")
    private Long categorySeq;


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

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

    /**
     * 카테고리 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 카테고리 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 상태를 CategoryStatus enum으로 반환
     */
    public CategoryStatus getStatus() {
        return isActive ? CategoryStatus.ACTIVE : CategoryStatus.INACTIVE;
    }

    /**
     * 기본 정보 업데이트
     */
    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}