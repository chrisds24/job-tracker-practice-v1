package com.chrisds24.job_tracker_practice_v1.auth;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.chrisds24.job_tracker_practice_v1.member.Member;

public class MemberUserDetails implements UserDetails {
    // What I want this to return is a UserDetails type of object
    //   that also has member

    // NOTE: Username and password are already in member
    // private String username;
    // private String password;

    private Member member;

    public MemberUserDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    // Note: Email is what I use as username
    @Override 
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public String getPassword() {
        return member.getPasswordHash();
    }

    // UserDetails requires me to implement getAuthorities (which Marco Behler
    //   doesn't show). Keeping it simple for now and just returning an
    //   empty list
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
}

// ***** This is what the UserDetails interface looks like
// public interface UserDetails extends Serializable { // (2)

//     String getUsername();

//     String getPassword();

//     // <3> more methods:
//     // isAccountNonExpired,isAccountNonLocked,
//     // isCredentialsNonExpired,isEnabled
// }
