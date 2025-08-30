package io.goorm.board.repository;

import io.goorm.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // JpaRepository<엔티티클래스, PK타입>
    // 기본 CRUD 메서드들이 자동으로 제공됨 - 구현 코드 작성 불필요!
}