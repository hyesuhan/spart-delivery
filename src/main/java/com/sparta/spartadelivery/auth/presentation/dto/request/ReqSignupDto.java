package com.sparta.spartadelivery.auth.presentation.dto.request;

import com.sparta.spartadelivery.user.domain.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReqSignupDto(

        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank(message = "사용자 이름은 필수입니다.")
        @Size(min = 2, max = 10, message = "사용자 이름은 2~10자여야 합니다.")
        String username,

        @Schema(description = "비밀번호. 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.", example = "Password1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,15}$",
                message = "비밀번호는 8~15자의 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password,

        @Schema(description = "사용자 닉네임", example = "고길동")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 100, message = "닉네임은 최대 100자까지 입력할 수 있습니다.")
        String nickname,

        @Schema(description = "로그인에 사용할 이메일", example = "user01@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 255, message = "이메일은 최대 255자까지 입력할 수 있습니다.")
        String email,

        @Schema(description = "사용자 권한", example = "CUSTOMER")
        @NotNull(message = "권한은 필수입니다.")
        Role role
) {
}
