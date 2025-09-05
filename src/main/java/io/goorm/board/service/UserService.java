package io.goorm.board.service;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.exception.DuplicateEmailException;
import io.goorm.board.exception.InvalidCredentialsException;
import io.goorm.board.exception.UserNotFoundException;
import io.goorm.board.repository.UserRepository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public User signup(SignupDto signupDto) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }
        
        // 비밀번호 확인 검사
        if (!signupDto.getPassword().equals(signupDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        // 사용자 엔티티 생성
        User user = new User();
        user.setEmail(signupDto.getEmail());
        user.setPassword(signupDto.getPassword()); // 평문 저장 (추후 암호화 예정)
        user.setNickname(signupDto.getNickname());
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User authenticate(LoginDto loginDto) {
        Optional<User> userOptional = userRepository.findByEmail(loginDto.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        User user = userOptional.get();
        
        // 평문 비밀번호 검증 (추후 암호화 예정)
        if (!user.getPassword().equals(loginDto.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        
        return user;
    }
    
    public User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 현재 비밀번호 확인 (평문 비교)
        if (!user.getPassword().equals(profileUpdateDto.getCurrentPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 닉네임 업데이트
        user.setNickname(profileUpdateDto.getNickname());
        
        // 새 비밀번호가 입력된 경우 비밀번호 변경
        if (profileUpdateDto.getNewPassword() != null && !profileUpdateDto.getNewPassword().isEmpty()) {
            // 새 비밀번호 확인 검사
            if (!profileUpdateDto.getNewPassword().equals(profileUpdateDto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            
            // 평문으로 저장 (추후 암호화 예정)
            user.setPassword(profileUpdateDto.getNewPassword());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }
}