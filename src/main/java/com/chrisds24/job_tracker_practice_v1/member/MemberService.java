package com.chrisds24.job_tracker_practice_v1.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(
        @Autowired MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void signup(SignupRequestDto newUser) {
        // First, ensure that email isn't taken
        // IMPORTANT: DO NOT return a Conflict status code that states the
        //   email has already been taken. That is a security risk
        // if (memberRepository.findByEmail(newUser.email()).isPresent()) {
        if (memberRepository.existsByEmail(newUser.email())) {
            // throw exception
        }

        // Doesn't really hash. Use for now and maybe add Bcrypt later
        String fakePasswordHash = newUser.password();

        memberRepository.save(MemberMapper.toEntity(
            newUser.name(),
            newUser.email(),
            fakePasswordHash
        ));
    }
}
