package io.goorm.board.service.impl;

import io.goorm.board.entity.Post;
import io.goorm.board.exception.PostNotFoundException;
import io.goorm.board.repository.PostRepository;
import io.goorm.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("postService")
@RequiredArgsConstructor
@Profile("jpa")
public class PostServiceJpaImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Post findBySeq(Long seq) {
        return postRepository.findById(seq)
                .orElseThrow(() -> new PostNotFoundException(seq));
    }

    @Override
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public Post findForEdit(Long seq) {
        return postRepository.findById(seq)
                .orElseThrow(() -> new PostNotFoundException(seq));
    }

    @Override
    @Transactional
    public void save(Post post) {
        postRepository.save(post);
    }

    @Override
    @Transactional
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public void update(Long seq, Post updatePost) {
        Post post = findBySeq(seq);

        post.setTitle(updatePost.getTitle());
        post.setContent(updatePost.getContent());
        // @Transactional에 의해 자동으로 UPDATE 쿼리 실행
    }

    @Override
    @Transactional
    @PreAuthorize("@postService.isOwner(#seq, authentication.name)")
    public void delete(Long seq) {
        postRepository.deleteById(seq);
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