package io.goorm.board.controller.rest.responseentity;

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

import static io.goorm.board.controller.rest.responseentity.ResponseEntityGlobalExceptionHandler.createSuccessResponse;

@Slf4j
@RestController
@RequestMapping("/api/responseentity/posts")
@RequiredArgsConstructor
public class ResponseEntityPostController {

    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        try {
            List<Post> posts = postService.findAll();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 게시글 상세 조회
    @GetMapping("/{seq}")
    public ResponseEntity<Post> getPost(@PathVariable Long seq) {
        try {
            Post post = postService.findBySeq(seq);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            log.error("게시글 조회 실패 - seq: {}", seq, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(@Valid @RequestBody Post post,
                                                         @AuthenticationPrincipal User user) {
        try {
            // 작성자 설정
            post.setAuthor(user);
            postService.save(post);

            // 정형화된 성공 응답
            Map<String, Object> response = createSuccessResponse("게시글이 성공적으로 작성되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("게시글 생성 실패", e);
            throw new RuntimeException("게시글 작성에 실패했습니다.", e);
        }
    }

    // 게시글 수정
    @PutMapping("/{seq}")
    public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long seq,
                                                         @Valid @RequestBody Post post) {
        try {
            postService.update(seq, post);

            // 정형화된 성공 응답 (추가 데이터 포함)
            Map<String, Object> data = Map.of("updatedSeq", seq);
            Map<String, Object> response = createSuccessResponse("게시글이 성공적으로 수정되었습니다.", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 수정 실패 - seq: {}", seq, e);
            throw new RuntimeException("게시글 수정에 실패했습니다.", e);
        }
    }

    // 게시글 삭제
    @DeleteMapping("/{seq}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long seq) {
        try {
            postService.delete(seq);

            // 정형화된 성공 응답 (추가 데이터 포함)
            Map<String, Object> data = Map.of("deletedSeq", seq);
            Map<String, Object> response = createSuccessResponse("게시글이 성공적으로 삭제되었습니다.", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 삭제 실패 - seq: {}", seq, e);
            throw new RuntimeException("게시글 삭제에 실패했습니다.", e);
        }
    }
}