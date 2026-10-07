package com.chrisds24.job_tracker_practice_v1.auth;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.chrisds24.job_tracker_practice_v1.member.Member;

// The purpose of this class is to have a UserDetails object that also
//   contains the member itself, not just the username, password, and
//   authorities
// - UserDetails: For an overly simplified explanation, a UserDetails is simply
//   an object representing your database user (in my case, member) that Spring
//   Security can understand
// - UserDetailsService is the one responsible for loading the user from the
//   database and mapping it into a UserDetails object
public class MemberUserDetails implements UserDetails {

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
    //
    // TODO: Fix this later after adding authorization
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
