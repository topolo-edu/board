package io.goorm.board.service;

import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 회원가입_성공_테스트() {
        // Given
        SignupDto signupDto = new SignupDto();
        signupDto.setUsername("testuser");
        signupDto.setPassword("testpass123");
        signupDto.setPasswordConfirm("testpass123");
        signupDto.setEmail("test@example.com");
        signupDto.setDisplayName("테스트유저");

        // When
        User savedUser = userService.signup(signupDto);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getDisplayName()).isEqualTo("테스트유저");
        assertThat(savedUser.getPassword()).isNotEqualTo("testpass123"); // 암호화되어야 함
    }

    @Test
    void 중복_사용자명_회원가입_실패_테스트() {
        // Given
        SignupDto firstUser = new SignupDto();
        firstUser.setUsername("duplicate");
        firstUser.setPassword("testpass123");
        firstUser.setPasswordConfirm("testpass123");
        firstUser.setEmail("first@example.com");
        firstUser.setDisplayName("첫번째유저");

        SignupDto secondUser = new SignupDto();
        secondUser.setUsername("duplicate"); // 중복된 사용자명
        secondUser.setPassword("testpass456");
        secondUser.setPasswordConfirm("testpass456");
        secondUser.setEmail("second@example.com");
        secondUser.setDisplayName("두번째유저");

        // When
        userService.signup(firstUser);

        // Then
        assertThatThrownBy(() -> userService.signup(secondUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 비밀번호_불일치_회원가입_실패_테스트() {
        // Given
        SignupDto signupDto = new SignupDto();
        signupDto.setUsername("testuser");
        signupDto.setPassword("testpass123");
        signupDto.setPasswordConfirm("differentpass"); // 비밀번호 불일치
        signupDto.setEmail("test@example.com");
        signupDto.setDisplayName("테스트유저");

        // When & Then
        assertThatThrownBy(() -> userService.signup(signupDto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}