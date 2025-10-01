package io.goorm.board.controller.rest.jwt;

import io.goorm.board.dto.ApiResponse;
import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/jwt/posts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")  // 클래스 레벨에 적용
@Tag(name = "Post API (JWT)", description = "JWT 기반 게시글 관련 API")
public class JwtPostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "모든 게시글을 조회합니다 (인증 불필요)")
    @SecurityRequirements()  // 클래스 레벨 요구사항 무시
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Post.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
        try {
            log.info("📋 JWT 게시글 목록 조회 시작");
            List<Post> posts = postService.findAll();
            log.info("📋 JWT 게시글 목록 조회 완료: {}개 게시글", posts.size());

            ApiResponse<List<Post>> response = ApiResponse.<List<Post>>builder()
                    .success(true)
                    .message("게시글 목록 조회 성공")
                    .data(posts)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ JWT 게시글 목록 조회 실패: {}", e.getMessage());

            ApiResponse<List<Post>> response = ApiResponse.<List<Post>>builder()
                    .success(false)
                    .message("게시글 목록 조회 실패: " + e.getMessage())
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글을 조회합니다 (인증 불필요)")
    @SecurityRequirements()  // 클래스 레벨 요구사항 무시
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Post.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{seq}")
    public ResponseEntity<ApiResponse<Post>> getPost(
            @Parameter(description = "게시글 번호") @PathVariable Long seq) {

        try {
            Post post = postService.findBySeq(seq);

            ApiResponse<Post> response = ApiResponse.<Post>builder()
                    .success(true)
                    .message("게시글 조회 성공")
                    .data(post)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("게시글 조회 실패 (seq: {}): {}", seq, e.getMessage());

            ApiResponse<Post> response = ApiResponse.<Post>builder()
                    .success(false)
                    .message("게시글을 찾을 수 없습니다.")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다 (JWT 토큰 필요)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공",
                    content = @Content(schema = @Schema(implementation = Post.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPost(
            @Parameter(description = "게시글 정보", required = true) @Valid @RequestBody Post post,
            @AuthenticationPrincipal User user) {
        log.info("✍️ JWT 게시글 작성 시작: 제목='{}', 작성자={}", post.getTitle(), user.getEmail());
        post.setAuthor(user);
        postService.save(post);
        log.info("✅ JWT 게시글 작성 완료: seq={}, 제목='{}'", post.getSeq(), post.getTitle());

        Map<String, Object> data = Map.of("success", true);
        ApiResponse<Map<String, Object>> response = ApiResponse.success("게시글 작성 성공", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(
            @Parameter(description = "게시글 번호", required = true) @PathVariable Long seq,
            @Parameter(description = "수정할 게시글 정보", required = true) @Valid @RequestBody Post post) {
        postService.update(seq, post);

        Map<String, Object> data = Map.of("updatedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success("게시글 수정 성공", data);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{seq}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(
            @Parameter(description = "게시글 번호", required = true) @PathVariable Long seq) {
        postService.delete(seq);

        Map<String, Object> data = Map.of("deletedSeq", seq);
        ApiResponse<Map<String, Object>> response = ApiResponse.success("게시글 삭제 성공", data);
        return ResponseEntity.ok(response);
    }
}