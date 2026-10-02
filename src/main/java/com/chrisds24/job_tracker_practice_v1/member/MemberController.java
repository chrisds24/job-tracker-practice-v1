package com.chrisds24.job_tracker_practice_v1.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    
    public MemberController(
        @Autowired MemberService memberService
    ) {
        this.memberService = memberService;
    }

    // Since I want to keep the response the same if email is already in
    //   use by another user or if it's not, I'll return a 202 Accepted here
    // - This is common when signup involves email verification and we don't
    //   want to expose the existence of a signed up email
    // - I can then send a simple response body with a message telling the
    //   user that they'll receive further instructions if the email
    //   can be registered
    // - NOTE: The normal status code for creating a resource is 201 Created,
    //   which I'm not using here for the reason mentioned above
    @PostMapping()
    // @ResponseStatus(HttpStatus.CREATED)
    @ResponseStatus(HttpStatus.ACCEPTED)
    // public void signup(
    public SignupResponseDto signup(
        @Valid @RequestBody SignupRequestDto newUser
    ) {
        memberService.signup(newUser);

        return new SignupResponseDto(
            "If this email can be registered, you'll receive further instructions."
        );
    }

    // 200 is the standard for a successful login
    @PostMapping()
    public LoginResponseDto login(
        @RequestBody LoginRequestDto credentials
    ) {
        return memberService.login(credentials);
    }
}
