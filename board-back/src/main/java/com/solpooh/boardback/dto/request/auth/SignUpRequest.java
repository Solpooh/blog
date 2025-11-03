package com.solpooh.boardback.dto.request.auth;
import jakarta.validation.constraints.*;
public record SignUpRequest(
    @NotBlank @Email
    String email,
    @NotBlank @Size(min = 8, max = 20)
    String password,
    @NotBlank
    String nickname,
    @NotBlank @Pattern(regexp = "^[0-9]{11,13}$")
    String telNumber,
    @NotBlank
    String address,
    String addressDetail,
    @NotNull @AssertTrue
    Boolean agreedPersonal
) {}
