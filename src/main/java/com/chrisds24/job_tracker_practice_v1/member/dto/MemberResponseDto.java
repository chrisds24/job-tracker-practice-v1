package com.chrisds24.job_tracker_practice_v1.member.dto;

import java.util.List;
import java.util.UUID;

public record MemberResponseDto(
    UUID id,
    String name,
    String email,
    List<String> roles

    // DO NOT return password/passwordHash
) {}
