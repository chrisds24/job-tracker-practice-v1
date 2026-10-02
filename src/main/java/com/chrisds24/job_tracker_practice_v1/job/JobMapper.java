package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.UUID;

import com.chrisds24.job_tracker_practice_v1.member.Member;

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
        //
        // IMPORTANT: Since the member field is LAZY, using
        //   job.getMember().getId() here doesn't initialize the job's
        //   member field
        // - The reason is that Hibernate already knows the member's id from
        //   the job's foreign key
        // - HOWEVER, doing something like job.getMember().getName() does load
        //   the member, which can cause N+1 queries when this mapping method
        //   is used in a loop
        return new JobResponseDto(
            job.getId(),
            job.getMember().getId(),
            job.getTitle(),
            job.getCompany(),
            job.getDateSaved(),
            job.getStatus(),
            job.getSalaryMin(),
            job.getSalaryMax()
        );
    }

    public static Job toEntity(
        Member member,
        CreateJobRequestDto newJob
    ) {
        return new Job(
            newJob.title(),
            newJob.company(),
            newJob.status(),
            newJob.salaryMin(),
            newJob.salaryMax(),
            member
        );
    }
}
