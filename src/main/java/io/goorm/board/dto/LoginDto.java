package io.goorm.board.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginDto {
    
    @NotBlank(message = "{validation.username.required}")
    private String username;
    
    @NotBlank(message = "{validation.password.required}")
    private String password;
    
    // 기본 생성자
    public LoginDto() {}
    
    // 생성자
    public LoginDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
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
}