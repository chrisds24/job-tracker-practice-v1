package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.UUID;

// https://docs.oracle.com/en/java/javase/17/language/records.html
record JobResponseDto(
    UUID id,
    UUID memberId,
    String title,
    String company,
    // When Jackson serializes the DTO to JSON, the Instant is represented
    //   as an ISO-8601 timestamp so there's no need to manually convert it
    //   to a String in that format
    Instant dateSaved,
    String status,
    Integer salaryMin,
    Integer salaryMax
) { }
