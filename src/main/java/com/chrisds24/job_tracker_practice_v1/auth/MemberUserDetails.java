package com.chrisds24.job_tracker_practice_v1.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private final Member member;

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

    // API: https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/core/userdetails/UserDetails.html#getAuthorities()
    //
    // NOTE: Marco Behler doesn't show a getAuthorities method in his tutorial
    // - HOWEVER, he shows that the authorities need to be a list of
    //   SimpleGrantedAuthority
    //
    // SimpleGrantedAuthority is simply a class that Spring Security uses
    //   to represent String authorities
    //
    // REMEMBER: Add the ROLE_ prefix here, since something like
    //   .requestMatchers("/admin/**").hasRole("ADMIN") requires the ROLE_
    //   prefix
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = member.getRoles();
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (String role: roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return grantedAuthorities;
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
