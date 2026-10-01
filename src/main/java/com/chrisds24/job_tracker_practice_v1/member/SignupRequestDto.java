package com.chrisds24.job_tracker_practice_v1.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record SignupRequestDto(
    @NotBlank
    String name,

    // Email can handle the min size
    @NotBlank
    @Email
    @Size(max = 254)
    String email,

    // NOTE: This is not a password hash
    @NotBlank
    @Size(min = 8, max = 128)
    String password
) {}
