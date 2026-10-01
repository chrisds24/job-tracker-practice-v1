package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// JobRepository is a Spring Data JPA repository for the Job entity whose
//   primary key type is UUID (So it's the same as Job's @Id field)
// - Because this extends JpaRepository, it inherits methods such as
//   findById, findAll, save, delete, etc.
// - Spring Data creates the repository implementation/proxy for you at
//   runtime so there's no need to write it yourself
// - I can also can add derived query methods and @Query methods inside
//   this interface as needed
public interface JobRepository extends JpaRepository<Job, UUID> {
    // *** Possible filters:
    // - memberId
    // - title
    // - company
    // - dateSavedFrom
    // - dateSavedTo
    // - status
    // - salaryMin
    // - salaryMax
    // 
    // For the query below (title example):
    // - If title is null, then the condition simply evaluates to true. But if
    //   it isn't, then j.title is checked against title
    //
    // Another option is to build the query dynamically using
    //   Spring Data JPA Specifications, but it's too much to do for a
    //   practice projectl like this
    //
    // If I'm using an @ManyToOne Member member field for Job instead of
    //   UUID memberId, then j.member.id should replace j.memberId below.
    // - Also, there's no risk of N+1 queries with the @ManyToOne member field
    //   using the query below since Hibernate understands that j.member.id
    //   is the foreign key and can use job's member_id in the database
    //
    // Projection vs. Convert to response dto in a loop
    // - The advantage of a projection is being able to return less fields if
    //   the use case doesn't require all of the resource's fields
    //   -- However, it's not any more efficient when it comes to converting
    //      the resource into a different representation that uses all of those
    //      fields anyway (Ex. Job -> JobResponseDto)
    //   -- The conversion happens anyway, so we can just do it as part of
    //      application code
    //
    // TODO: Don't forget to use Sort, Page, Pageable, etc. later
    @Query("""
        SELECT j
        FROM Job j
        WHERE (:memberId IS NULL OR j.memberId = :memberId)
            AND (:title IS NULL OR j.title = :title)
            AND (:company IS NULL OR j.company = :company)
            AND (:dateSavedFrom IS NULL OR j.dateSaved >= :dateSavedFrom)
            AND (:dateSavedTo IS NULL OR j.dateSaved <= :dateSavedTo)
            AND (:status IS NULL OR j.status = :status)
            AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin)
            AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax)
    """)
    List<Job> findJobs(
        @Param("memberId") UUID memberId,
        @Param("title") String title,
        @Param("company") String company,
        @Param("dateSavedFrom") Instant dateSavedFrom,
        @Param("dateSavedTo") Instant dateSavedTo, 
        @Param("status") String status,
        @Param("salaryMin") Integer salaryMin,
        @Param("salaryMax") Integer salaryMax
    );
}
