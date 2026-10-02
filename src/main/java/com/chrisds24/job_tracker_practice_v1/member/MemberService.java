package com.chrisds24.job_tracker_practice_v1.member;

import java.util.Optional;

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

    @Transactional
    public LoginResponseDto login(LoginRequestDto credentials) {
        // First, find member with email
        Optional<Member> optionalMember = memberRepository.findByEmail(
            credentials.email()
        );
        Member member = optionalMember.isPresent() ?
            optionalMember.get() :
            null;
        if (member == null) { // Member with email doesn't exist
            return null; // Later, throw an exception instead
        }

        // Then, hash the provided password and compare with the loaded
        //   member's hashed password
        // - NOTE: This is using a fake hashed password, which isn't even hashed
        //   at all in this case
        // - Normally, I can use BCrypt to hash and verify passwords
        String fakePasswordHash = credentials.password();
        if (fakePasswordHash != member.getPasswordHash()) {
            return null; // Again, throw an exception instead later
        }

        // Create the JWT then return it along with the member's details
        // - NOTE: This is again just using a fake JWT.
        // - Normally, I can use a JWT library to generate the JWT
        String jwt = "fakeJwt";
        return MemberMapper.toLoginResponseDto(jwt, member);
    }
}
