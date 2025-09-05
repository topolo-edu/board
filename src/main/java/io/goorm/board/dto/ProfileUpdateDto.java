package io.goorm.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDto {
    
    @NotBlank(message = "{validation.nickname.required}")
    @Size(min = 2, max = 20, message = "{validation.nickname.size}")
    private String nickname;
    
    @NotBlank(message = "{validation.password.current.required}")
    private String currentPassword;
    
    // 새 비밀번호는 선택사항
    @Size(min = 4, message = "{validation.password.size}")
    private String newPassword;
    
    private String newPasswordConfirm;
}