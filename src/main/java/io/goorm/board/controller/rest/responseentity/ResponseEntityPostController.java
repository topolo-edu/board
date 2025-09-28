package io.goorm.board.controller.rest.responseentity;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/responseentity/posts")
@RequiredArgsConstructor
public class ResponseEntityPostController {

    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.findAll();
        return ResponseEntity.ok(posts);
    }

    // 게시글 상세 조회
    @GetMapping("/{seq}")
    public ResponseEntity<Post> getPost(@PathVariable Long seq) {
        Post post = postService.findBySeq(seq);
        return ResponseEntity.ok(post);
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPost(@Valid @RequestBody Post post,
                                                        @AuthenticationPrincipal User user) {
        post.setAuthor(user);
        postService.save(post);

        ApiResponse<Void> response = ApiResponse.success("게시글이 성공적으로 작성되었습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정
    @PutMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(@PathVariable Long seq,
                                                                       @Valid @RequestBody Post post) {
        postService.update(seq, post);

        Map<String, Object> data = Map.of("updatedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success("게시글이 성공적으로 수정되었습니다.", data);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(@PathVariable Long seq) {
        postService.delete(seq);

        Map<String, Object> data = Map.of("deletedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success("게시글이 성공적으로 삭제되었습니다.", data);
        return ResponseEntity.ok(response);
    }
}