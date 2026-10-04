package com.chrisds24.job_tracker_practice_v1.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(
        @Autowired MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }
}
