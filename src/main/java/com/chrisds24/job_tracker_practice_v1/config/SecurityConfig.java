package com.chrisds24.job_tracker_practice_v1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;


@Configuration
@EnableTransactionManagement(rollbackOn=RollbackOn.ALL_EXCEPTIONS)
// - To rollback on all exceptions, not just runtime
public class SecurityConfig {
    // NOTE: Refer to login in AuthService.java for an explanation of where
    //   passwordEncoder and authenticationManager are used

    // Use the interface type PasswordEncoder instead of BCryptPasswordEncoder
    //   as the return value. If we later changed the implementation to a
    //   a different encoder, then our services that use it won't need to
    //   change.
    @Bean
    // public BCryptPasswordEncoder bCryptPasswordEncoder() {
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Login method in AuthService uses the authenticate method from this
    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // I actually don't need this if I annotate CustomUserDetailsService
    //   as a @Service
    // @Bean
    // public UserDetailsService userDetailsService() {
    //     return new CustomUserDetailsService();
    // }
    
}
