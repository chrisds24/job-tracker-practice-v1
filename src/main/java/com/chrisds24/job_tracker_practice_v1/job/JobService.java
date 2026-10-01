package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class JobService {
    private final JobRepository jobRepository;

    public JobService(@Autowired JobRepository jobRepository) {
        this.jobRepository = jobRepository;
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
        for (Job job : jobs) {
            jobResDtos.add(JobMapper.toResponseDto(job));
        }
        return jobResDtos;
    }

    @Transactional(readOnly = true)
    public JobResponseDto getJob(UUID id) {
        Optional<Job> job = jobRepository.findById(id);
        // TODO: Throw an exception that a global exception handler can
        //   handle instead
        return job.isEmpty() ? null : JobMapper.toResponseDto(job.get());
    }

    @Transactional
    public JobResponseDto createJob(
        UUID memberId,
        CreateJobRequestDto newJob
    ) {
        validateSalaryRange(newJob.salaryMin(), newJob.salaryMax());

        // Look at CrudRepository documentation for save()
        // NOTE: save doesn't return null upon persistence failure
        // - Instead, an exception is thrown. Keep in mind that the exception
        //   could be thrown long after save since it could happen during flush
        //   instead
        return JobMapper.toResponseDto(
            jobRepository.save(JobMapper.toEntity(memberId, newJob))
        );
    }

    // TODO: Change to return void later, since it should instead throw an
    //   exception when no job is found
    //
    // NOTE: @Modifying belongs to modifying @Query methods, not service methods
    @Transactional
    public JobResponseDto editJob(
        UUID id,
        EditJobRequestDto editJob
    ) {
        // First, validate the salary range provided by the user
        validateSalaryRange(editJob.salaryMin(), editJob.salaryMax());

        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            // TODO: Throw an exception
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
        // Since job is already persisted, there is no need to save just to
        //   return the entity returned by save
        return JobMapper.toResponseDto(
            jobRepository.save(job)
        );
    }

    // The inherited repository deleteById method actually returns void.
    // - Even if the job can't be found, it is counted as a success and no
    //   exception is thrown.
    //   -- However, an exception is still thrown if there's database
    //      constraint issues, etc.
    // - Though, I can create a derived query that returns the number of
    //   entities deleted.
    // - Otherwise, I need to use findById first, then call delete.
    @Transactional
    public void deleteJob(
        UUID id
    ) {
        jobRepository.deleteById(id);
    }

    private void validateSalaryRange(
        Integer salaryMin,
        Integer salaryMax
    ) {
        // Only validate range when both are present
        if (salaryMin != null && salaryMax != null) {
            if (salaryMin > salaryMax) {
                // throw an exception
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
