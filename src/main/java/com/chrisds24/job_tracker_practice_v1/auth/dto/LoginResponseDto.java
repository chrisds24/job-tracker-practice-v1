package com.chrisds24.job_tracker_practice_v1.auth.dto;

import com.chrisds24.job_tracker_practice_v1.member.dto.MemberResponseDto;

public record LoginResponseDto(
    String jwt,
    MemberResponseDto member
) {}
