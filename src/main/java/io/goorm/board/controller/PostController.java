package io.goorm.board.controller;

import io.goorm.board.entity.Post;
import io.goorm.board.entity.User;
import io.goorm.board.exception.AccessDeniedException;
import io.goorm.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 메인 페이지
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // 게시글 목록 (기본)
    @GetMapping("/posts")
    public String list(Model model) {
        List<Post> posts = postService.findAll();

        model.addAttribute("posts", posts);
        return "post/list";
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{seq}")
    public String show(@PathVariable Long seq, Model model) {
        Post post = postService.findBySeq(seq);
        model.addAttribute("post", post);
        return "post/show";
    }

    // 게시글 작성 폼
    @GetMapping("/posts/new")
    @PreAuthorize("isAuthenticated()")
    public String createForm(Model model) {
        model.addAttribute("post", new Post());
        return "post/form";
    }

    // 게시글 저장 → 목록으로
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public String create(@Valid @ModelAttribute Post post, 
                        BindingResult bindingResult,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes redirectAttributes) {
        
        // 검증 오류가 있으면 폼으로 다시 이동
        if (bindingResult.hasErrors()) {
            return "post/form";
        }
        
        // 작성자 설정
        post.setAuthor(user);
        
        // 검증 통과 시에만 저장
        postService.save(post);
        redirectAttributes.addFlashAttribute("message", "flash.post.created");
        return "redirect:/posts";
    }

    // 게시글 수정 폼
    @GetMapping("/posts/{seq}/edit")
    @PreAuthorize("hasPermission(#seq, 'Post', 'WRITE')")
    public String editForm(@PathVariable Long seq, Model model) {
        Post post = postService.findBySeq(seq);
        model.addAttribute("post", post);
        return "post/form";
    }

    // 게시글 수정 → 상세보기로
    @PostMapping("/posts/{seq}")
    public String update(@PathVariable Long seq, 
                        @Valid @ModelAttribute Post post, 
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        
        // 검증 오류가 있으면 폼으로 다시 이동
        if (bindingResult.hasErrors()) {
            post.setSeq(seq); // seq 값 설정 (수정 폼에서 필요)
            return "post/form";
        }
        
        // 서비스에서 @PreAuthorize로 권한 체크
        postService.update(seq, post);
        redirectAttributes.addFlashAttribute("message", "flash.post.updated");
        return "redirect:/posts/" + seq;
    }

    // 게시글 삭제 → 목록으로
    @PostMapping("/posts/{seq}/delete")
    public String delete(@PathVariable Long seq, RedirectAttributes redirectAttributes) {
        
        // 서비스에서 @PreAuthorize로 권한 체크
        postService.delete(seq);
        redirectAttributes.addFlashAttribute("message", "flash.post.deleted");
        return "redirect:/posts";
    }


}
