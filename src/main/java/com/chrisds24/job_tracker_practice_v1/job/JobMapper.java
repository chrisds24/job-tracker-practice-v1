package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.UUID;

public class JobMapper {
    public static JobResponseDto toResponseDto(
        Job job
    ) {
        // ------- JobResponseDto fields ------
        // UUID id,
        // UUID memberId,
        // String title,
        // String company,
        // Instant dateSaved,
        // String status,
        // Integer salaryMin,
        // Integer salaryMax
        return new JobResponseDto(
            job.getId(),
            job.getMemberId(),
            job.getTitle(),
            job.getCompany(),
            job.getDateSaved(),
            job.getStatus(),
            job.getSalaryMin(),
            job.getSalaryMax()
        );
    }

    public static Job toEntity(
        UUID memberId,
        CreateJobRequestDto newJob
    ) {
        return new Job(
            memberId,
            newJob.title(),
            newJob.company(),
            newJob.status(),
            newJob.salaryMin(),
            newJob.salaryMax()
        );
    }
}
