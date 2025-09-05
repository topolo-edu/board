package io.goorm.board.service;

import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.exception.DuplicateEmailException;
import io.goorm.board.repository.UserRepository;
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
}