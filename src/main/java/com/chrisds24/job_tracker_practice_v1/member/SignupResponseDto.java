package com.chrisds24.job_tracker_practice_v1.member;

// Need public, otherwise simply having "record SignupResponseDto"
//   means its only visible to classes in the same package
public record SignupResponseDto(
    String message
) {}
