package io.goorm.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDto {
    
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;
    
    @NotBlank(message = "{validation.displayname.required}")
    @Size(min = 2, max = 20, message = "{validation.displayname.size}")
    private String displayName;
    
    // 비밀번호 변경 관련 (선택사항)
    private String currentPassword;
    
    @Size(min = 8, message = "{validation.password.size}")
    private String newPassword;
    
    private String newPasswordConfirm;
    
    // 기본 생성자
    public ProfileUpdateDto() {}
    
    // Getter와 Setter
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
    
    public String getCurrentPassword() {
        return currentPassword;
    }
    
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }
    
    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }
    
    // 비즈니스 메서드
    public boolean isPasswordChangeRequested() {
        return currentPassword != null && !currentPassword.trim().isEmpty() &&
               newPassword != null && !newPassword.trim().isEmpty();
    }
    
    public boolean isNewPasswordMatched() {
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}