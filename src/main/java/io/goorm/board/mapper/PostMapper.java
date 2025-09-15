package io.goorm.board.mapper;

import io.goorm.board.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    /**
     * 모든 게시글 조회 (작성자 정보 포함)
     */
    List<Post> findAll();

    /**
     * ID로 게시글 조회 (작성자 정보 포함)
     */
    Post findBySeq(@Param("seq") Long seq);

    /**
     * 게시글 저장
     */
    void save(Post post);

    /**
     * 게시글 수정
     */
    void update(Post post);

    /**
     * 게시글 삭제
     */
    void deleteBySeq(@Param("seq") Long seq);

    /**
     * 조회수 증가
     */
    void incrementViewCount(@Param("seq") Long seq);
}