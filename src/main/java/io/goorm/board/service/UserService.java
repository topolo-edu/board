package io.goorm.board.service;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.User;
import io.goorm.board.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }
    
    /**
     * 회원가입 처리
     */
    public User signup(SignupDto signupDto) {
        // 1. 비밀번호 확인 검증
        if (!signupDto.isPasswordMatched()) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.password.mismatch", null, LocaleContextHolder.getLocale()));
        }
        
        // 2. 중복 확인
        validateDuplicateUser(signupDto);
        
        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupDto.getPassword());
        
        // 4. 사용자 엔티티 생성 및 저장
        User user = new User(
            signupDto.getUsername(),
            encodedPassword,
            signupDto.getEmail(),
            signupDto.getDisplayName()
        );
        
        return userRepository.save(user);
    }
    
    /**
     * 로그인 인증 처리
     */
    @Transactional(readOnly = true)
    public User authenticate(LoginDto loginDto) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(loginDto.getUsername())
            .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.auth.invalid.credentials", null, LocaleContextHolder.getLocale())));
        
        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.invalid.credentials", null, LocaleContextHolder.getLocale()));
        }
        
        return user;
    }
    
    /**
     * 프로필 업데이트
     */
    public User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("error.auth.user.notfound", null, LocaleContextHolder.getLocale())));
        
        // 2. 중복 확인 (현재 사용자 제외)
        validateDuplicateForUpdate(userId, profileUpdateDto);
        
        // 3. 비밀번호 변경 요청 처리
        if (profileUpdateDto.isPasswordChangeRequested()) {
            processPasswordChange(user, profileUpdateDto);
        }
        
        // 4. 기본 정보 업데이트
        user.updateProfile(profileUpdateDto.getEmail(), profileUpdateDto.getDisplayName());
        
        return userRepository.save(user);
    }
    
    /**
     * 사용자 조회 (ID로)
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    /**
     * 사용자 조회 (사용자명으로)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // === Private Methods ===
    
    /**
     * 회원가입 시 중복 검증
     */
    private void validateDuplicateUser(SignupDto signupDto) {
        if (userRepository.existsByUsername(signupDto.getUsername())) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.username.duplicate", null, LocaleContextHolder.getLocale()));
        }
        
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.email.duplicate", null, LocaleContextHolder.getLocale()));
        }
        
        if (userRepository.existsByDisplayName(signupDto.getDisplayName())) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.displayname.duplicate", null, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * 프로필 수정 시 중복 검증 (현재 사용자 제외)
     */
    private void validateDuplicateForUpdate(Long userId, ProfileUpdateDto profileUpdateDto) {
        if (userRepository.existsByEmailAndIdNot(profileUpdateDto.getEmail(), userId)) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.email.duplicate", null, LocaleContextHolder.getLocale()));
        }
        
        if (userRepository.existsByDisplayNameAndIdNot(profileUpdateDto.getDisplayName(), userId)) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.displayname.duplicate", null, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * 비밀번호 변경 처리
     */
    private void processPasswordChange(User user, ProfileUpdateDto profileUpdateDto) {
        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(profileUpdateDto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.password.current.invalid", null, LocaleContextHolder.getLocale()));
        }
        
        // 2. 새 비밀번호 확인 검증
        if (!profileUpdateDto.isNewPasswordMatched()) {
            throw new IllegalArgumentException(messageSource.getMessage("error.auth.password.new.mismatch", null, LocaleContextHolder.getLocale()));
        }
        
        // 3. 새 비밀번호 암호화 및 업데이트
        String encodedNewPassword = passwordEncoder.encode(profileUpdateDto.getNewPassword());
        user.updatePassword(encodedNewPassword);
    }
}