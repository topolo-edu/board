package io.goorm.board.service.impl;

import io.goorm.board.entity.Post;
import io.goorm.board.exception.PostNotFoundException;
import io.goorm.board.mapper.PostMapper;
import io.goorm.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("postService")
@RequiredArgsConstructor
@Profile("mybatis")
public class PostServiceMyBatisImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public List<Post> findAll() {
        return postMapper.findAll();
    }

    @Override
    @Transactional
    public Post findBySeq(Long seq) {
        Post post = postMapper.findBySeq(seq);
        if (post == null) {
            throw new PostNotFoundException(seq);
        }

        // 조회수 증가
        postMapper.incrementViewCount(seq);

        return post;
    }

    @Override
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public Post findForEdit(Long seq) {
        Post post = postMapper.findBySeq(seq);
        if (post == null) {
            throw new PostNotFoundException(seq);
        }
        return post;
    }

    @Override
    @Transactional
    public void save(Post post) {
        postMapper.save(post);
    }

    @Override
    @Transactional
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public void update(Long seq, Post updatePost) {
        // 게시글 존재 확인
        Post existingPost = findBySeq(seq);

        // seq 설정 후 업데이트
        updatePost.setSeq(seq);
        postMapper.update(updatePost);
    }

    @Override
    @Transactional
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public void delete(Long seq) {
        // 게시글 존재 확인
        findBySeq(seq);

        postMapper.deleteBySeq(seq);
    }

    @Override
    public boolean isOwner(Long seq, String email) {
        try {
            Post post = findBySeq(seq);
            return post.getAuthor().getEmail().equals(email);
        } catch (Exception e) {
            return false;
        }
    }
}