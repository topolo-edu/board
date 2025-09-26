package io.goorm.board.controller.rest.json;

import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.entity.Company;
import io.goorm.board.entity.User;
import io.goorm.board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/json/users")
@RequiredArgsConstructor
public class JsonUserController {

    private final UserService userService;

    // 사용자 상세 조회
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    // 현재 로그인한 사용자 정보
    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal User user) {
        return user;
    }

    // 회사 목록 조회
    @GetMapping("/companies")
    public List<Company> getAllCompanies() {
        return userService.findAllCompanies();
    }

    // 사용자 정보 수정 (자신의 정보만)
    @PutMapping("/me")
    public User updateMyProfile(@AuthenticationPrincipal User currentUser,
                               @Valid @RequestBody ProfileUpdateDto profileUpdateDto) {

        // 현재 사용자의 정보만 수정 가능
        return userService.updateProfile(currentUser.getId(), profileUpdateDto);
    }
}