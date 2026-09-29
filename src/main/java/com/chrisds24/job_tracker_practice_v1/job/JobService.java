package com.chrisds24.job_tracker_practice_v1.job;

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
    public List<JobResponseDto> getJobs() {
        // TODO: Call JPA 
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
        Optional<Job> optionalJob = jobRepository.findById(id);
        if (optionalJob.isEmpty()) {
            // TODO: Throw an exception
        }

        Job job = optionalJob.get();

        // TODO: Create an updateEntity method in JobMapper to keep this
        //   service method clean
    }

    @Transactional
    public void deleteJob(
        UUID id
    ) {
        jobRepository.deleteById(id);
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
