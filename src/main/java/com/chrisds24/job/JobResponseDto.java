package com.chrisds24.job;

import java.time.Instant;
import java.util.UUID;

// https://docs.oracle.com/en/java/javase/17/language/records.html
record JobResponseDto(
    UUID string,
    UUID memberId,
    String title,
    String company,
    // TODO: Convert this to a more appropriate type
    Instant dateSaved
) { }
