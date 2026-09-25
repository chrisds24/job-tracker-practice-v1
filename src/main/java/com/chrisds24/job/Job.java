package com.chrisds24.job;

import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// NOTE: We also want job DTOs since we don't want to be using our JPA entities
//   for data transfer, since the JPA entities are for representing database
//   persistence / database models
// - We define DTOs as part of the HTTP API contract since we want the API and
//   its consumers to agree with the types of what's being transferred, which
//   shouldn't be coupled with how the database models look like
//   -- In other words, the way we represent objects in our database isn't
//      necessarily the same as how we transfer data about those objects

@Entity
@Table(name = "job")
public class Job {
    // Job
    // ------------
    // id
    // memberId
    // title
    // company
    // dateSaved
    @Id
    @Generated
    @ColumnDefault("gen_random_uuid()")
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    // For the entity's dateSaved field, Instant is the correct type to use if
    //   the actual Postgres column is "date_saved TIMESTAMPTZ NOT NULL DEFAULT
    //   NOW()"
    // - TIMESTAMPTZ represents a specific point in time. PostgreSQL normalizes
    //   the value internally to UTC and converts it for display according to
    //   the session timezone.
    // - Instant also represents a specific point on the UTC timeline
    // Also, since PostgreSQL is generating the value with DEFAULT NOW(), you
    //   may want Hibernate to treat the column as database-generated rather
    //   than supplying it during the INSERT.
    // - A simple approach could be to add this annotation:
        // @Column(
        //     name = "date_saved",
        //     nullable = false,
        //     insertable = false,
        //     updatable = false
        // )
    @Column(name = "date_saved", nullable = false)
    @Generated
    @ColumnDefault("NOW()")
    private Instant dateSaved;

    // TODO: lastUpdated
    
    public Job(
        UUID id,
        UUID memberId,
        String title,
        String company,
        Instant dateSaved
    ) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.company = company;
        this.dateSaved = dateSaved;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getMemberId() { return memberId; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public Instant getDateSaved() { return dateSaved; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }
    public void setTitle(String title) { this.title = title; }
    public void setCompany(String company) { this.company = company; }
    public void setDateSaved(Instant dateSaved) { this.dateSaved = dateSaved; }
}
