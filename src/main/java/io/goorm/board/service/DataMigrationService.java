package io.goorm.board.service;

import io.goorm.board.entity.User;
import io.goorm.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataMigrationService {
    
    private final UserRepository userRepository;
    
    @Bean
    @Transactional
    public ApplicationRunner createAnonymousUser() {
        return args -> {
            // 익명사용자가 이미 존재하는지 확인
            if (!userRepository.existsByEmail("anonymous@example.com")) {
                User anonymousUser = new User();
                anonymousUser.setEmail("anonymous@example.com");
                anonymousUser.setPassword("temp123");
                anonymousUser.setNickname("익명사용자");
                userRepository.save(anonymousUser);
                
                System.out.println("익명사용자 계정이 생성되었습니다: anonymous@example.com");
            }
        };
    }
}