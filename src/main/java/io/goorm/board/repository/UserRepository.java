package io.goorm.board.repository;

import io.goorm.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 사용자명 중복 확인
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임 중복 확인
     */
    boolean existsByDisplayName(String displayName);
    
    /**
     * 특정 사용자 ID를 제외한 이메일 중복 확인 (프로필 수정 시 사용)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
    
    /**
     * 특정 사용자 ID를 제외한 닉네임 중복 확인 (프로필 수정 시 사용)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.displayName = :displayName AND u.id != :userId")
    boolean existsByDisplayNameAndIdNot(@Param("displayName") String displayName, @Param("userId") Long userId);
}