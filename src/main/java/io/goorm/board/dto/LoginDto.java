package io.goorm.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청 정보")
public class LoginDto {
    
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    @Email(message = "{validation.email.invalid}")
    @NotBlank(message = "{validation.email.required}")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "password123", required = true)
    @NotBlank(message = "{validation.password.required}")
    private String password;
}