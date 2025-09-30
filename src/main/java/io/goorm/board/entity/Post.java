package io.goorm.board.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity  // JPA 엔티티임을 나타냄
@Table(name = "posts")  // 실제 테이블명 지정
@Getter @Setter  // Lombok: getter/setter 자동 생성
@NoArgsConstructor  // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor  // Lombok: 모든 필드 생성자 자동 생성
@ToString  // Lombok: toString() 메서드 자동 생성
@Schema(description = "게시글 정보")
public class Post {

    @Schema(description = "게시글 번호", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @Id  // Primary Key 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가
    private Long seq;  // 게시글 번호

    @Schema(description = "게시글 제목", example = "Spring Boot 학습하기", required = true, maxLength = 200)
    @NotBlank(message = "{post.title.required}")
    @Size(min = 1, max = 200, message = "{post.title.size}")
    @Column(nullable = false, length = 200)
    private String title;  // 제목

    @Schema(description = "게시글 내용", example = "Spring Boot에 대해 학습한 내용을 정리합니다...", required = true, maxLength = 4000)
    @NotBlank(message = "{post.content.required}")
    @Size(min = 10, max = 4000, message = "{post.content.size}")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 내용

    @Schema(description = "게시글 작성자 정보", accessMode = Schema.AccessMode.READ_ONLY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    private User author;  // 작성자

    @Schema(description = "조회수", example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount  = 0L;  //조회수

    @Schema(description = "작성일시", example = "2024-01-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    @CreationTimestamp  // 자동으로 현재 시간 입력
    @Column(updatable = false)  // 수정 불가
    private LocalDateTime createdAt;  // 작성일시
}