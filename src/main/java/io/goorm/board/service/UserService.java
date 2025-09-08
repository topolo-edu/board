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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User signup(SignupDto signupDto) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new DuplicateEmailException(signupDto.getEmail());
        }
        
        // 비밀번호 확인 검사
        if (!signupDto.getPassword().equals(signupDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Password mismatch");
        }
        
        // 사용자 엔티티 생성
        User user = new User();
        user.setEmail(signupDto.getEmail());
        user.setPassword(passwordEncoder.encode(signupDto.getPassword())); // BCrypt 암호화
        user.setNickname(signupDto.getNickname());
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User authenticate(LoginDto loginDto) {
        Optional<User> userOptional = userRepository.findByEmail(loginDto.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException(loginDto.getEmail());
        }
        
        User user = userOptional.get();
        
        // 평문 비밀번호 검증 (추후 암호화 예정)
        if (!user.getPassword().equals(loginDto.getPassword())) {
            throw new InvalidCredentialsException(loginDto.getEmail());
        }
        
        return user;
    }
    
    public User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 현재 비밀번호 확인 (BCrypt 비교)
        if (!passwordEncoder.matches(profileUpdateDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(user.getEmail());
        }
        
        // 닉네임 업데이트
        user.setNickname(profileUpdateDto.getNickname());
        
        // 새 비밀번호가 입력된 경우 비밀번호 변경
        if (profileUpdateDto.getNewPassword() != null && !profileUpdateDto.getNewPassword().isEmpty()) {
            // 새 비밀번호 확인 검사
            if (!profileUpdateDto.getNewPassword().equals(profileUpdateDto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("Password mismatch");
            }
            
            // BCrypt로 암호화하여 저장
            user.setPassword(passwordEncoder.encode(profileUpdateDto.getNewPassword()));
        }
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    // UserDetailsService 구현 - Spring Security에서 사용
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}