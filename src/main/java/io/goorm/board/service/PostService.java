package io.goorm.board.service;

import io.goorm.board.entity.Post;
import io.goorm.board.exception.PostNotFoundException;
import io.goorm.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service  // Spring Service Bean으로 등록
@RequiredArgsConstructor  // Lombok: final 필드에 대한 생성자 자동 생성
public class PostService {

    private final PostRepository postRepository;  // 의존성 주입

    // 전체 게시글 조회
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // SEQ로 게시글 조회
    public Post findBySeq(Long seq) {
        return postRepository.findById(seq)
                .orElseThrow(() -> new PostNotFoundException(seq));
    }

    // 게시글 저장
    @Transactional  // 쓰기 작업은 별도 트랜잭션
    public Post save(Post post) {
        return postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public Post update(Long seq, Post updatePost) {
        Post post = findBySeq(seq);
        post.setTitle(updatePost.getTitle());
        post.setContent(updatePost.getContent());
        // 작성자는 수정하지 않음 (본인 글만 수정 가능하므로)
        return post;  // @Transactional에 의해 자동으로 UPDATE 쿼리 실행
    }

    // 게시글 삭제
    @Transactional
    public void delete(Long seq) {
        // 삭제 전 게시글 존재 여부 확인
        Post post = findBySeq(seq);  // 없으면 PostNotFoundException 발생
        postRepository.deleteById(seq);
    }
}