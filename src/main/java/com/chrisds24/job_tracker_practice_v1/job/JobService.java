package com.chrisds24.job_tracker_practice_v1.job;

import java.util.List;
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
    public List<JobResponseDto> getJobs(UUID memberId) {
        // TODO: Call JPA 
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
