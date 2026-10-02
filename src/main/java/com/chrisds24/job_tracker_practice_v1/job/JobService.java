package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisds24.job_tracker_practice_v1.job.exception.InvalidSalaryRangeException;
import com.chrisds24.job_tracker_practice_v1.job.exception.JobNotFoundException;
import com.chrisds24.job_tracker_practice_v1.member.MemberRepository;

@Service 
public class JobService {
    private final JobRepository jobRepository;
    private final MemberRepository memberRepository;

    public JobService(
        @Autowired JobRepository jobRepository,
        @Autowired MemberRepository memberRepository
    ) {
        this.jobRepository = jobRepository;
        this.memberRepository = memberRepository;
    }


    // @Transactional(readOnly = true) is used to optimize database
    //   transactions that only retrieve data
    @Transactional(readOnly = true)
    public List<JobResponseDto> getJobs(
        UUID memberId,
        String title,
        String company,
        Instant dateSavedFrom,
        Instant dateSavedTo,
        String status,
        Integer salaryMin,
        Integer salaryMax        
    ) {
        validateSalaryRange(salaryMin, salaryMax);

        // IMPORTANT: Exception Handling
        // - If no jobs are returned (empty list), that is not an exception
        // - Also, if something goes wrong in the query executed by findJobs,
        //   that is an unexpected database error, network error, etc., so a
        //   fallback handler returning 500 status is appropriate for this case
        // - Basically, there is no need to wrap every repository method call
        //   with try-catch just to deal with database errors
        List<Job> jobs = jobRepository.findJobs(
            memberId,
            title,
            company,
            dateSavedFrom,
            dateSavedTo,
            status,
            salaryMin,
            salaryMax
        );

        List<JobResponseDto> jobResDtos = new ArrayList<>();
        // No risk of N+1 query here since Job's member field is LAZY
        //   and toResponseDto only gets the member's id, which doesn't
        //   cause the member to be loaded
        // - Another note: If all jobs belong to the same member.
        //   Hibernate's persistence context allows that member to be loaded
        //   once and then reused.
        //   -- BUT DON'T rely purely on this just to avoid N+1 queries
        for (Job job : jobs) {
            jobResDtos.add(JobMapper.toResponseDto(job));
        }
        return jobResDtos;
    }

    @Transactional(readOnly = true)
    public JobResponseDto getJob(UUID id) {
        Optional<Job> job = jobRepository.findById(id);
        if (job.isEmpty()) {
            throw new JobNotFoundException(
                "Job with id " + id + " not found."
            );
        }
        return JobMapper.toResponseDto(job.get());
    }

    @Transactional
    public JobResponseDto createJob(
        UUID memberId,
        CreateJobRequestDto newJob
    ) {
        validateSalaryRange(newJob.salaryMin(), newJob.salaryMax());

        // IMPORTANT: Since I'm representing the Job-Member relationship
        //   in JPA via an @ManyToOne member field in Job, I'll need to
        //   get the member so I can pass it to the Job constructor
        // - In actuality, I don't really need the full member itself and I
        //   only need a reference to it
        // - Therefore, I can use getReferenceById which gives me a lazy
        //   reference of the member
        //   -- NOTE: I need to ensure that my authentication guarantees the
        //      existence of this member, since if the member doesn't actually
        //      exist, it can cause a foreign-key violation when the job is
        //      inserted
        //      + The reason for this is that getReferenceById() can still
        //        return a Member reference even if that id doesn't actually
        //        exist in the database
        //        * And the reason behind this is that this method simply gives
        //          us a reference to the entity with the provided id and DOES
        //          NOT actually query the database to verify if the resource
        //          exists
        //   -- This provides the benefit of not needing a SELECT to get the
        //      member
        //
        // Also, looking at CrudRepository documentation for save(), it
        //   doesn't return null upon persistence failure
        // - Instead, an exception is thrown. Keep in mind that the exception
        //   could be thrown long after save since it could happen during flush
        //   instead
        return JobMapper.toResponseDto(
            jobRepository.save(
                JobMapper.toEntity(
                    memberRepository.getReferenceById(memberId), newJob
                )
            )
        );

        // IMPORTANT: Exception handling
        // - If save fails, an exception is thrown
        // - But since errors caused by save are usually unexpected such as
        //   SQL errors, network errors, etc., it's fine to just let the
        //   fallback exception handler handle it
        // - Also, the SQL execution happens outside of save and after it, so
        //   wrapping save with a try-catch is not helpful in this case
        // - NOTE: As mentioned above, performing the SQL using a non-existent
        //   memberId is a foreign key violation. However, since we expect the
        //   authentication to do its job properly and ensure that the memberId
        //   here actually exists, then a non-existent memberId here can be
        //   treated as an "unexpected" case and its sensible to just leave it
        //   to the fallback handler even though it's a foreign key violation
        //   (which can either be a 409 or 422 error status)
    }

    // This method doesn't allow changing a job's associated member since
    //   jobs only belong to one member upon creation
    //
    // There's no inherited repository method from JpaRepository to update a
    //   resource. We simply load the resource, then the flush automatically
    //   executes an UPDATE query that DOESN'T return how many entities are
    //   updated. (Even save() doesn't return the number of entities)
    // - If I wanted to return how many entities are updated, I'd need to use
    //   an explicit @Modifying query
    //
    // NOTE: @Modifying belongs to modifying @Query methods, not service methods
    @Transactional
    public void editJob(
        UUID id,
        EditJobRequestDto editJob
    ) {
        // First, validate the salary range provided by the user
        validateSalaryRange(editJob.salaryMin(), editJob.salaryMax());

        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            throw new JobNotFoundException("Job not found");
        }

        Job job = optionalJob.get();
        if (editJob.title() != null) {
            job.setTitle(editJob.title());
        }
        if (editJob.company() != null) {
            job.setCompany(editJob.company());
        }
        if (editJob.status() != null) {
            job.setStatus(editJob.status());
        }

        if (editJob.salaryMin() != null) {
            job.setSalaryMin(editJob.salaryMin());
        }
        if (editJob.salaryMax() != null) {
            job.setSalaryMax(editJob.salaryMax());
        }
        // Validate the range again in case the user only provided one
        //   salary-related field and the new value causes an invalid
        //   salary range
        validateSalaryRange(job.getSalaryMin(), job.getSalaryMax());

        // Dirty-checking ensures that the job is updated in the database
        //   without explicitly calling save and/or flush
        // - NOTE: If returning the job to the controller:
        //   -- Since job is already persisted, there is no need to save just
        //      to get the entity returned by save. I can just convert job
        //      itself to the DTO representation

        // IMPORTANT: Exception handling
        // - Since the the flush happens automatically some time after the
        //   method body has finished, how should I handle exceptions caused
        //   by SQL errors, network errors, etc.?
        //   -- SOLUTION: Just have the fallback handler handle it. There, I
        //      can log the original cause then just send something like
        //      "Unexpected error" in the error message being sent to the
        //      client. A 500 status code works fine for this case
    }

    // The inherited repository deleteById method actually returns void.
    // - Even if the job can't be found, it is counted as a success and no
    //   exception is thrown.
    //   -- This isn't a problem since the purpose of a delete is to simply
    //      ensure that the resource is deleted
    //   -- However, an exception is still thrown if there's database
    //      constraint issues, etc.
    // If I need the number of jobs deleted:
    // - I can create a derived query that returns the number of entities
    //   deleted.
    // - Otherwise, I need to use findById first, then call delete.
    @Transactional
    public void deleteJob(
        UUID id
    ) {
        jobRepository.deleteById(id);

        // IMPORTANT: Exception handling
        // - I'm just letting the fallback exception handler deal with
        //   exceptions that happen here since those exceptions
        //   are unexpected unlike something like JobNotFoundException
    }

    private void validateSalaryRange(
        Integer salaryMin,
        Integer salaryMax
    ) {
        // Only validate range when both are present
        if (salaryMin != null && salaryMax != null) {
            if (salaryMin > salaryMax) {
                throw new InvalidSalaryRangeException("Salary min > salary max");
            }
        }
    }
}

// *************** NOTES ****************
// We add @Transactional to service methods where the whole logic needs
//   to run within one transaction. Ex. In createJob, where not only do we
//   update the job table itself, but also the resume/cover letter tables
//   if we also add files to that job.
// - Another example is editJob. Without the @Transactional on this service
//   method, the transaction used internally by findById() doesn't remain
//   active for the rest of editJob.
//   -- Therefore, dirty-checking depends on having this service-level
//      transaction
