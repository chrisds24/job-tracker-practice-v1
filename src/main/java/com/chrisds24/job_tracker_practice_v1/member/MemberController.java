package com.chrisds24.job_tracker_practice_v1.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    
    public MemberController(
        @Autowired MemberService memberService
    ) {
        this.memberService = memberService;
    }

    // NOTE: Login and signup features are moved to auth folder
}
