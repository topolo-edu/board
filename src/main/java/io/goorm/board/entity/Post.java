package io.goorm.board.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity  // JPA 엔티티임을 나타냄
@Table(name = "posts")  // 실제 테이블명 지정
@Getter @Setter  // Lombok: getter/setter 자동 생성
@NoArgsConstructor  // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor  // Lombok: 모든 필드 생성자 자동 생성
@ToString  // Lombok: toString() 메서드 자동 생성
public class Post {

    @Id  // Primary Key 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 자동 증가
    private Long seq;  // 게시글 번호

    @Column(nullable = false, length = 200)
    private String title;  // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 내용

    @Column(nullable = false, length = 50)
    private String author;  // 작성자

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount  = 0L;  //조회수

    @CreationTimestamp  // 자동으로 현재 시간 입력
    @Column(updatable = false)  // 수정 불가
    private LocalDateTime createdAt;  // 작성일시
}