package io.goorm.board.dto;

import jakarta.validation.constraints.*;

public class SignupDto {
    
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 4, max = 20, message = "{validation.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.username.pattern}")
    private String username;
    
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, message = "{validation.password.size}")
    private String password;
    
    @NotBlank(message = "{validation.password.confirm.required}")
    private String passwordConfirm;
    
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;
    
    @NotBlank(message = "{validation.displayname.required}")
    @Size(min = 2, max = 20, message = "{validation.displayname.size}")
    private String displayName;
    
    // 기본 생성자
    public SignupDto() {}
    
    // Getter와 Setter
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
    
    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
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
    
    // 비즈니스 메서드
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}