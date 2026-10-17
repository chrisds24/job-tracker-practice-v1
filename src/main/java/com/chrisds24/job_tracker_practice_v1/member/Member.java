package com.chrisds24.job_tracker_practice_v1.member;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Generated;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Use type TEXT[] in PostgreSQL
    // NOTE: nullable = false prevents a SQL NULL value, but doesn't prevent
    //   an empty array. So requiring every Member to have at least one role
    //   should be enforced in signup logic.
    // There's no need to use @JdbcTypeCode(SqlTypes.JSON) here
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> roles = new ArrayList<>();

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
    //
    // Also, I don't need functionality such as member.getJobs(), so there's no
    //   need to have this.
    // @OneToMany(fetch = FetchType.LAZY)
    // private List<Job> jobs = new ArrayList<>();

    protected Member() {}

    public Member(
        String name,
        String email,
        String passwordHash,
        List<String> roles
    ) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public List<String> getRoles() { return roles; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
