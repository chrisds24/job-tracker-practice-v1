package com.chrisds24.job_tracker_practice_v1.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping()
    public ResponseEntity<Void> signup(
        @RequestBody SignupRequestDto newUser
    ) {
        memberService.signup(newUser);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .build();
    }

    @PostMapping()
    public ResponseEntity<LoginResponseDto> login(
        @RequestBody LoginRequestDto credentials
    ) {
        
    }
}
