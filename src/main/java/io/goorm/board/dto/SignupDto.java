package io.goorm.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {
    
    @Email(message = "{validation.email.invalid}")
    @NotBlank(message = "{validation.email.required}")
    private String email;
    
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 4, message = "{validation.password.size}")
    private String password;
    
    @NotBlank(message = "{validation.password.confirm.required}")
    private String passwordConfirm;
    
    @NotBlank(message = "{validation.nickname.required}")
    @Size(min = 2, max = 20, message = "{validation.nickname.size}")
    private String nickname;
}