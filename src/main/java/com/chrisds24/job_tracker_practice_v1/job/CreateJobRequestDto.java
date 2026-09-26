package com.chrisds24.job_tracker_practice_v1.job;

// NOTE: We have a CreateJobRequestDto instead of just a JobResponseDto for
//   everything since what a user is allowed to send is usually different
//   from what they receive
// - We can even go further and have different request DTOs for different
//   operations, such as a CreateJobRequestDto vs. an EditJobRequestDto
record CreateJobRequestDto(
    String title,
    String company,
    String status
) { }
