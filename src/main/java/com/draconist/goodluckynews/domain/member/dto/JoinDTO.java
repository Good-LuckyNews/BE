package com.draconist.goodluckynews.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinDTO {

    @NotBlank(message = "이메일 입력은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "이메일은 '뭐시기@뭐시기.뭐시기' 형식이어야 합니다."
    )
    private String email;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름 입력은 필수입니다.")
    private String name;

    private String profileImage;

    @NotBlank(message = "오전/오후 값은 필수입니다.")
    @Pattern(regexp = "AM|PM", message = "'AM' 또는 'PM'만 입력 가능합니다.")
    private String amPm;

    private int hours;

    private int minutes;
}

