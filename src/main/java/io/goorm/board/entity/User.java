package io.goorm.board.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 4, max = 20, message = "{validation.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.username.pattern}")
    private String username;
    
    @Column(nullable = false)
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, message = "{validation.password.size}")
    private String password;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;
    
    @Column(nullable = false, length = 20)
    @NotBlank(message = "{validation.displayname.required}")
    @Size(min = 2, max = 20, message = "{validation.displayname.size}")
    private String displayName;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    protected User() {}
    
    // 생성자
    public User(String username, String password, String email, String displayName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.displayName = displayName;
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // 비즈니스 메서드
    public void updateProfile(String email, String displayName) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email.trim();
        }
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName.trim();
        }
    }
    
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}