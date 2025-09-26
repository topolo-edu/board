package io.goorm.board.controller.rest.json;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/json/posts")
@RequiredArgsConstructor
public class JsonPostController {

    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    // 페이징 지원 게시글 목록 조회
    @GetMapping("/paged")
    public Page<Post> getPagedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return postService.findAll(pageable);
    }

    // 게시글 상세 조회
    @GetMapping("/{seq}")
    public Post getPost(@PathVariable Long seq) {
        return postService.findBySeq(seq);
    }

    // 게시글 생성
    @PostMapping
    public Post createPost(@Valid @RequestBody Post post,
                          @AuthenticationPrincipal User user) {

        // 작성자 설정
        post.setAuthor(user);
        return postService.save(post);
    }

    // 게시글 수정
    @PutMapping("/{seq}")
    public Post updatePost(@PathVariable Long seq,
                          @Valid @RequestBody Post post) {

        return postService.update(seq, post);
    }

    // 게시글 삭제
    @DeleteMapping("/{seq}")
    public Map<String, Object> deletePost(@PathVariable Long seq) {
        postService.delete(seq); // 서비스 메서드는 void 반환이므로 컨트롤러에서 응답 생성

        // 삭제 성공 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "게시글이 성공적으로 삭제되었습니다.");
        response.put("deletedSeq", seq);
        return response;
    }

    // 제목으로 검색
    @GetMapping("/search")
    public List<Post> searchPosts(@RequestParam String title) {
        return postService.findByTitleContaining(title);
    }

    // 작성자별 게시글 조회
    @GetMapping("/author/{authorId}")
    public List<Post> getPostsByAuthor(@PathVariable Long authorId) {
        return postService.findByAuthorId(authorId);
    }
}