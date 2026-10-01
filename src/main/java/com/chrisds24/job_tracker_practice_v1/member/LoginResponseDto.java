package com.chrisds24.job_tracker_practice_v1.member;

record LoginResponseDto(
    String jwt,
    MemberResponseDto member
) {}
