package io.goorm.board.service;

import io.goorm.board.entity.Post;

import java.util.List;

public interface PostService {

    /**
     * 전체 게시글 조회
     */
    List<Post> findAll();

    /**
     * SEQ로 게시글 조회 (읽기 전용)
     */
    Post findBySeq(Long seq);

    /**
     * 수정용 게시글 조회 (권한 체크 포함)
     */
    Post findForEdit(Long seq);

    /**
     * 게시글 저장
     */
    void save(Post post);

    /**
     * 게시글 수정
     */
    void update(Long seq, Post updatePost);

    /**
     * 게시글 삭제
     */
    void delete(Long seq);

    /**
     * 게시글 소유자 확인
     */
    boolean isOwner(Long seq, String email);
}