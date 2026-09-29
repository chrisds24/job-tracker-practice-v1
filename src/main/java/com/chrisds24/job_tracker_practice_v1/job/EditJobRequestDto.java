package com.chrisds24.job_tracker_practice_v1.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// IMPORTANT: In my use case, null means changing the value to null
// - Omitting the field means don't change it
record EditJobRequestDto(
    @Size(max = 255)
    String title,

    @Size(max = 255)
    String company,

    // TODO: Change this to an enum later
    String status,

    @PositiveOrZero
    Integer salaryMin,

    @PositiveOrZero 
    Integer salaryMax
) {}
