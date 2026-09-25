package com.chrisds24.job_tracker_practice_v1.member;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Generated;

import com.chrisds24.job_tracker_practice_v1.job.Job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "member")
public class Member {
    // Member
    // - id
    // - name
    // - email
    // - passwordHash

    @Id
    @Generated
    private UUID id;

    @Column(nullable = false)
    private String name;

    // unique = true in @Column
    // - Not needed since Hibernate isn't generating my schema
    @Column(nullable = false)
    private String email;

    // This represents the hashed password from the database
    // IMPORTANT: DO NOT expose this field in DTOs
    @Column(nullable = false)
    private String passwordHash;

    // When Hibernate loads a Member from the database, Hibernate populates the
    //   collection appropriately. The new ArrayList<>() initialization is
    //   mainly helpful for new Java objects before Hibernate has managed the
    //   relationship
    // - member.getJobs().add(job)  will work here for a newly created Member
    //   object (transient).
    // - On the contrary, this would fail if we only had:
    //     private List<Job> jobs;
    //
    // NOTE: @OneToMany is LAZY by default, but it's good to be explicit here
    @OneToMany(fetch = FetchType.LAZY)
    private List<Job> jobs = new ArrayList<>();
}
