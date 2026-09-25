package com.chrisds24.job;

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

    @Transactional(readOnly = true)
    public List<JobResponseDto> getMultipleByUser(UUID memberId) {
        // TODO: Call JPA 
    }
}
