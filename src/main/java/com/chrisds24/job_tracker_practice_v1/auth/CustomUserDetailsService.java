package com.chrisds24.job_tracker_practice_v1.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.chrisds24.job_tracker_practice_v1.member.Member;
import com.chrisds24.job_tracker_practice_v1.member.MemberRepository;

// This is a custom UserDetailsService implementation so that
//   loadUserByUsername returns a custom UserDetails object (MemberUserDetails)
//   that also contains a member
@Service 
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(
        @Autowired MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    // Here, username is the email
    // Basically, we just want this function to return a UserDetails object
    //   if the member can be found. Otherwise, it needs to throw
    //   a UsernameNotFoundException
    // The interface has a "throws UsernameNotFoundException", but this
    //   exception is unchecked so there's no need to write it here as long as
    //   the method throws the exception when the user isn't found
    @Override 
    public UserDetails loadUserByUsername(String username) {
        Optional<Member> optionalMember = memberRepository.findByEmail(
            username
        );
        Member member = optionalMember.isPresent() ?
            optionalMember.get() :
            null;
        if (member == null) { // Member with email doesn't exist
            throw new UsernameNotFoundException(
                "Can't find member with given email"
            );
        }

        return new MemberUserDetails(member);
    }
}
