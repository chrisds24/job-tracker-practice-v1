package com.chrisds24.job_tracker_practice_v1.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// IMPORTANT: In my use case, null here means that the field isn't meant to be
//   changed.
// - It could be that the client didn't supply a value for the field or the
//   client explicitly supplied null
// So how should a field be cleared?
// - They need to explicitly enter an empty value for the field
// - For a nullable string value (Ex. description), it can just be an
//   empty string
//   -- A required field like title can't be cleared
// - But for something like salaryMin, 0 doesn't mean empty. So the strategy
//   above doesn't work
// - NOTE: Look into JsonNullable as a solution to this
record EditJobRequestDto(
    // We want to be able to have this field null.
    //
    // However, when it's supplied, it can't be an empty string
    //
    // @Size(min = 1) can deal with ensuring that the supplied value isn't
    //   an empty string. However, it can't deal with the case where
    //   the string is entirely whitespace
    // 
    // The regexp below matches non-empty strings that don't consist only
    //   whitespace
    @Pattern(regexp = ".*\\S.*")
    @Size(max = 255)
    String title,

    @Size(max = 255)
    @Pattern(regexp = ".*\\S.*")
    String company,

    // TODO: Change this to an enum later
    String status,

    @PositiveOrZero
    Integer salaryMin,

    @PositiveOrZero 
    Integer salaryMax
) {}
