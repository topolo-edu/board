package io.goorm.board.controller.rest.json;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/json/posts")
@RequiredArgsConstructor
public class JsonPostController {

    private final PostService postService;
    private final MessageSource messageSource;

    // 게시글 목록 조회
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    // 게시글 상세 조회
    @GetMapping("/{seq}")
    public Post getPost(@PathVariable Long seq) {
        return postService.findBySeq(seq);
    }

    // 게시글 생성
    @PostMapping
    public Map<String, Object> createPost(@Valid @RequestBody Post post,
                                         @AuthenticationPrincipal User user) {
        // 작성자 설정
        post.setAuthor(user);
        postService.save(post);

        // 다국어 메시지 조회
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.create.success", null, locale);

        // 성공 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    // 게시글 수정
    @PutMapping("/{seq}")
    public Map<String, Object> updatePost(@PathVariable Long seq,
                                         @Valid @RequestBody Post post) {
        postService.update(seq, post); // 서비스에서 예외 처리

        // 다국어 메시지 조회
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.update.success", null, locale);

        // 성공 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("updatedSeq", seq);
        return response;
    }

    // 게시글 삭제
    @DeleteMapping("/{seq}")
    public Map<String, Object> deletePost(@PathVariable Long seq) {
        postService.delete(seq); // 서비스에서 예외 처리

        // 다국어 메시지 조회
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.delete.success", null, locale);

        // 삭제 성공 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("deletedSeq", seq);
        return response;
    }
}