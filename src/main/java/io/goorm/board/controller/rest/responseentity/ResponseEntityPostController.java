package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/responseentity/posts")
@RequiredArgsConstructor
public class ResponseEntityPostController {

    private final PostService postService;
    private final MessageSource messageSource;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
        List<Post> posts = postService.findAll();

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.list.success", null, locale);

        ApiResponse<List<Post>> response = ApiResponse.success(message, posts);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세 조회
    @GetMapping("/{seq}")
    public ResponseEntity<ApiResponse<Post>> getPost(@PathVariable Long seq) {
        Post post = postService.findBySeq(seq); // 서비스에서 예외 처리

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.detail.success", null, locale);

        ApiResponse<Post> response = ApiResponse.success(message, post);
        return ResponseEntity.ok(response);
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPost(@Valid @RequestBody Post post,
                                                        @AuthenticationPrincipal User user) {
        post.setAuthor(user);
        postService.save(post);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.create.success", null, locale);

        Map<String, Object> data = Map.of("success", true);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정
    @PutMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(@PathVariable Long seq,
                                                                       @Valid @RequestBody Post post) {
        postService.update(seq, post); // 서비스에서 예외 처리

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.update.success", null, locale);

        Map<String, Object> data = Map.of("updatedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(@PathVariable Long seq) {
        postService.delete(seq); // 서비스에서 예외 처리

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("post.delete.success", null, locale);

        Map<String, Object> data = Map.of("deletedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }
}