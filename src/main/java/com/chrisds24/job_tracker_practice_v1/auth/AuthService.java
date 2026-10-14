package com.chrisds24.job_tracker_practice_v1.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrisds24.job_tracker_practice_v1.auth.dto.LoginRequestDto;
import com.chrisds24.job_tracker_practice_v1.auth.dto.LoginResponseDto;
import com.chrisds24.job_tracker_practice_v1.auth.dto.SignupRequestDto;
import com.chrisds24.job_tracker_practice_v1.auth.exception.EmailAlreadyExistsException;
import com.chrisds24.job_tracker_practice_v1.auth.exception.InvalidCredentialsException;
import com.chrisds24.job_tracker_practice_v1.member.Member;
import com.chrisds24.job_tracker_practice_v1.member.MemberMapper;
import com.chrisds24.job_tracker_practice_v1.member.MemberRepository;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final AuthenticationManager authenticationManager;

    public AuthService(
        @Autowired MemberRepository memberRepository,
        @Autowired AuthenticationManager authenticationManager
    ) {
        this.memberRepository = memberRepository;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void signup(SignupRequestDto newUser) {
        // First, ensure that email isn't taken
        //
        // IMPORTANT: DO NOT return a Conflict status code that states the
        //   email has already been taken. That is a security risk
        //
        // NOTE: Even though successful signup and an email already exists
        //   exception return the same response body and status code, I want
        //   to handle it in the exception handler to keep things consistent
        //   since this is an exception use case here
        //
        // if (memberRepository.findByEmail(newUser.email()).isPresent()) {
        if (memberRepository.existsByEmail(newUser.email())) {
            throw new EmailAlreadyExistsException(
                "Provided email is already in use by another user."
            );
        }

        // TODO: Use the BCryptPasswordEncoder bean later
        String fakePasswordHash = newUser.password();

        memberRepository.save(MemberMapper.toEntity(
            newUser.name(),
            newUser.email(),
            fakePasswordHash
        ));
    }

    // SEE EXPLANATION BELOW
    @Transactional
    public LoginResponseDto login(LoginRequestDto credentials) {
        // Authenticate with AuthenticationManager
        // - This throws AuthenticationException if unsuccessful
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.email(),
                credentials.password()
            )
        );

        // Get the member from the principal
        Member member =
            ((MemberUserDetails) authentication.getPrincipal())
                .getMember();

        // Create the JWT then return it along with the member's details
        // TODO: I probably want more info included in the JWT other than just
        //   id and email
        String jwt = jwtService.generateToken(
            member.getId(),
            member.getEmail()
        );
        return MemberMapper.toLoginResponseDto(jwt, member);
    }   

    // OLD LOGIN CODE LOGIC WITHOUT SPRING SECURITY
    // @Transactional
    // public LoginResponseDto login(LoginRequestDto credentials) {
    //     // First, find member with email
    //     Optional<Member> optionalMember = memberRepository.findByEmail(
    //         credentials.email()
    //     );
    //     Member member = optionalMember.isPresent() ?
    //         optionalMember.get() :
    //         null;
    //     if (member == null) { // Member with email doesn't exist
    //         throw new InvalidCredentialsException(
    //             "Invalid email or password"
    //         );
    //     }

    //     // Then, hash the provided password and compare with the loaded
    //     //   member's hashed password
    //     // - NOTE: This is using a fake hashed password, which isn't even hashed
    //     //   at all in this case
    //     // - Normally, I can use BCrypt to hash and verify passwords
    //     String fakePasswordHash = credentials.password();
    //     // Don't use == when comparing Strings
    //     if (fakePasswordHash.equals(member.getPasswordHash())) {
    //         throw new InvalidCredentialsException(
    //             "Invalid email or password"
    //         );
    //     }

    //     // Create the JWT then return it along with the member's details
    //     // - NOTE: This is again just using a fake JWT.
    //     // - Normally, I can use a JWT library to generate the JWT
    //     String jwt = "fakeJwt";
    //     return MemberMapper.toLoginResponseDto(jwt, member);
    // }   
}


/*
------------------------------ LOGIN EXPLANATION ----------------------------

Source for authenticating with an AuthenticationManager:
- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html
- Regarding DaoAuthenticationProvider:
    -- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html?utm_source=chatgpt.com
        + This one shows the flow of how a username and password gets used

AuthenticationManager
    Authentication
    AuthenticationManager's authenticate method
    UsernamePasswordAuthenticationToken
        unauthenticated vs authenticated
    ProviderManager

Authentication Provider
    DaoAuthenticationProvider
        UserDetailsService
            loadUserByUsername
            UserDetails
            CustomUserDetailsService (my own implementation)
        PasswordEncoder

Authentication.getPrincipal()

1. AuthenticationManager, authenticate method, Authentication, and
  UsernamePasswordAuthenticationToken
- Spring Security uses the AuthenticationManager interface to authenticate
  Authentication objects.
  -- authenticate(): We simply pass an unauthenticated Authentication object to
     its authenticate method.
     + On successful authentication, it returns an authenticated Authentication
       object which contains the principal and their authorities
     + Then on failure, it throws an AuthenticationException
  -- ProviderManager is one implementation of an AuthenticationManager
- Authentication is the general interface used to represent authentication
  info.
  -- UsernamePasswordAuthenticationToken is one implementation used for
     username/password-based authentication
     + ***** EXTRA DETAILS *****
     + Though it has the word "Token" in it, it's not a token in the same
       manner as a JWT
     + A UsernamePasswordAuthenticationToken is used to represent both
       the authenticated and unauthenticate state

2. ProviderManager
- An implementation of AuthenticationManager which is the one responsible
  for choosing an appropriate AuthenticationProvider that can authenticate
  the given Authentication object
  -- When it sees that you passed a UsernamePasswordAuthenticationToken, it
     knows that it should delegate to a DaoAuthenticationProvider to perform
     the authentication
- I don't call this myself during login. Instead, I interact with the
  ProviderManager through the AuthenticationManager interface

3. AuthenticationProvider
- AuthenticationProvider is the generic Spring Security interface used to
  represent different providers that can perform their own type of
  authentication
  -- As mentioned above, DaoAuthenticationProvider is one example of this
  -- Marco Behler also had an example regarding having a third-party service
     be responsible for storing user details and passwords.
     + For example, he created an AuthenticationProvider @Bean which returned
       an AtlassianCrowdAuthenticationProvider, a custom implementation of
       AuthenticationProvider

4. DaoAuthenticationProvider, UserDetailsService, UserDetails, PasswordEncoder,
   BCryptPasswordEncoder
- The AuthenticationProvider implementation used to authenticate
  username/password-based Authentication where your users' username/passwords
  are stored in your database
  -- Spring Security can actually configure one for us so there's no need to
     write a bean for this ourselves, unlike with a third-party provider
     (Ex. AtlassianAuthenticationProvider) where we need to create bean and
     write the implementation ourselves
- It uses a UserDetailsService to load the user then uses the PasswordEncoder
  to compare the stored password hash with the supplied password
  -- UserDetailsService is a Spring Security interface used to load a user's
     authentication info by username (via its loadUserByUsername method), then
     returns it as a UserDetails object
     + We actually need to write a custom UserDetailsService implementation
       so the loadUserByUsername method can use our own logic to load the job
       from our database and convert it to a UserDetails object
     + Notice how UserDetailsService acts as a mapper of some sort
     + loadUserByUsername does what was mentioned about it above
       * But one extra detail is that it needs to throw
         UsernameNotFoundException if the user can't be found in our database
       * NOTE: username can be an email
         ++ Basically, whatever identifier your authentication system treats
            as the username
  -- UserDetails: A Spring Security interface that represents the user
     information Spring Security needs, such as username, password, authorities,
     and others such asaccount status.
     + We can use Spring Security's implementation called
       User (org.springframework.security.core.userdetail.User) or implement
       our own so that the UserDetails object can contain other info we might
       need
       * Notice how Spring Security only requires us to implement the
         getUsername, getPassword, and getAuthorities methods, which will
         return all the info it needs.
         ++ However, we can also have our custom UserDetails implementation
            return other info in case we need it (such as accessing those info
            from the principal, which we can get from a successfully
            authenticated Authentication)
            ** Example: We need member from the the principal so we can
               use it along with the generated JWT to create a LoginResponseDto
               that we'll return to the client
            ** IMPORTANT: The UserDetails object is the principal
  -- PasswordEncoder: A Spring Security interface for encoding passwords and
     verifying raw passwords against stored password hashes
     + An example implementation is BCryptPasswordEncoder, which is used
       to utilize the bcrypt password hashing algorithm
     + Two important operations are:
       * encode(rawPassword)
       * matches(rawPassword, encodedPassword)
     + Notice how we don't hash the passwords ourselves and compare the
       Strings since the PasswordEncoder does that for us

5. Authentication's getPrincipal method
- The principal represents the identity being authenticated. After a successful
  username/password authentication, getPrincipal() returns the authenticated
  UserDetails
  -- NOTE: Before authentication, the principal is the username for a
     UsernamePasswordAuthenticationToken
     + So in my case, it's the email
*/
