package com.chrisds24.job_tracker_practice_v1.job;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

// JobRepository is a Spring Data JPA repository for the Job entity whose
//   primary key type is UUID (So it's the same as Job's @Id field)
// - Because this extends JpaRepository, it inherits methods such as
//   findById, findAll, save, delete, etc.
// - Spring Data creates the repository implementation/proxy for you at
//   runtime so there's no need to write it yourself
// - I can also can add derived query methods and @Query methods inside
//   this interface as needed
public interface JobRepository extends JpaRepository<Job, UUID> {
    
}
