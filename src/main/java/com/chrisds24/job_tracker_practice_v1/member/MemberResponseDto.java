package com.chrisds24.job_tracker_practice_v1.member;

import java.util.UUID;

record MemberResponseDto(
    UUID id,
    String name,
    String email

    // DO NOT return password/passwordHash
) {}
