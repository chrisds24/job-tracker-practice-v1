package com.chrisds24.job_tracker_practice_v1.member;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    // Optional<Member> findByEmail(String email);

    // Use existsBy when you only need to check existence
    boolean existsByEmail(String email);
}
