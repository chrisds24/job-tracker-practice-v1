package com.chrisds24.job_tracker_practice_v1.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// NOTE: We have a CreateJobRequestDto instead of just a JobResponseDto for
//   everything since what a user is allowed to send is usually different
//   from what they receive
// - We can even go further and have different request DTOs for different
//   operations, such as a CreateJobRequestDto vs. an EditJobRequestDto
//
// https://jakarta.ee/learn/docs/jakartaee-tutorial/current/beanvalidation/bean-validation/bean-validation.html
// - Source for Jakarta Bean Validation
record CreateJobRequestDto(
    @NotBlank
    @Size(max = 255)
    String title,

    @NotBlank
    @Size(max = 255)
    String company,

    // TODO: Change this to an enum later
    @NotBlank
    String status,

    // This annotation allows null, which is what we want since this isn't
    //   a required field unlke title, company, and status
    @PositiveOrZero
    Integer salaryMin,

    @PositiveOrZero 
    Integer salaryMax
) { }
