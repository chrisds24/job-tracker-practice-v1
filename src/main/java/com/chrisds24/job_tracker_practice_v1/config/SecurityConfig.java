package com.chrisds24.job_tracker_practice_v1.config;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;


@Configuration
@EnableMethodSecurity
// - To enable using annotations on methods to do authorization checks
//   -- Ex. @PreAuthorize, @PostAuthorize, @PreFilter, and @PostFilter
//   -- Source: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
//   -- Note that @Secured is now considered legacy 
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
    
    // https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-authorization-extraction
    // - "Spring Security ships with JwtAuthenticationConverter, which is
    //    responsible for converting a Jwt into an Authentication"
    // - However, I need to configure my own since my JWTs will have a
    //   "roles" attribute instead of "scope" and I need the authority prefix
    //   to be "ROLE_" instead of "SCOPE_".
    //   -- ***** Role vs Scope:
    //      + Role is something like USER vs ADMIN
    //      + Scope is something like read only, write only, etc. permissions
    //
    // ***** Example JWT:
        // {
        //     "sub": "member-uuid",
        //     "roles": ["USER", "ADMIN"],
        //     "iat": 1792000000,
        //     "exp": 1792003600
        // }
    // - IMPORTANT: The JWT should contain the expected claims above
    //   -- sub: subject -> identifies the member (UUID)
    //   -- iss: issuer —> identifies my backend (since it's the issuer in
    //      my use case, not an external authorization server)
    //      + I DIDN'T ACTUALLY INCLUDE THIS, but just putting the explanation
    //        here
    //   -- iat: issued at —> when the JWT was generated
    //   -- exp: expiration —> when the JWT expires
    // - I can also add my own attributes such as roles
    // - ***** KEEP IN MIND THAT roles embedded in existing JWTs can become
    //   outdated when permissions change
    // - Spring Security will create authorities such as:
    //          new SimpleGrantedAuthority("ROLE_USER");
    //   -- The JWTs must contain role names without the ROLE_ prefix.
    //      Otherwise, we'll end up with ROLE_ROLE_USER
    //   -- ***** This means that when I store roles in my database, they
    //      shouldn't have the ROLE_ prefix
    //      + This is appropriate since having the ROLE_ prefix is a Spring
    //        Security convention for representing role-based authorities
    //      + NOTE: In MemberUserDetails, the getAuthorities() method should
    //        return authorities with a ROLE_ prefix
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    
    // ***** IMPORTANT: I'm using Spring Security's OAuth 2.0 Resource Server
    //   features for JWT validation, but NOT to implement an OAuth 2.0 flow.
    //
    // By default, Spring Boot auto-configures and enables features like form
    //   login.
    // - However, defining your custom SecurityFilterChain @Bean overrides the
    //   default settings so there's no need to explicitly disable it
    // - Other defaults that I should disable:
    //   -- CSRF protection, Basic Authentication, logout form, sessions
    // - I should use SessionCreationPolicy.STATELESS since Spring Security
    //   uses sessions by default, which I want to disable
    //   -- With STATELESS, Spring Security doesn't use an HTTP session
    //      to persist the SecurityContext between requests
    //
    // Since I'm building a stateless REST API, CSRF protection doesn't make
    //   sense for my use case
    //
    // login and signup endpoints shouldn't require authentication, which
    //   is why I'm using permitAll here
    //
    // Refer to the notes in jwtSecretKey below for info on how this bean
    //   knows that it should use the jwtAuthenticationConverter bean above
    //
    // IMPORTANT: Rule ordering matter here since Spring Security evaluates
    //   authorization rules in their declared order and uses the first
    //   matching rule
    // - So don't put .anyRequest().authenticated() as the first rule
    //
    // IMPORTANT: The Authentication in the SecurityContextHolder is a
    //   JwtAuthenticationToken, which has a Jwt object as the principal
    //
    // Sources for editing SecurityFilterChain:
    // - https://docs.spring.io/spring-security/reference/servlet/configuration/java.html
    // - https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
    //
    // TODO: CORS
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http,
        JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http
            .csrf((csrf) -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers(
                    HttpMethod.POST,
                    "/auth/login",
                    "/auth/signup"
                ).permitAll()

                // Admin endpoints
                // - I won't actually have admin endpoints for this practice
                //   project, but I'll leave this here to demonstrate
                .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                
                // We use a wildcard pattern here so that anything in the
                //   /job and /member endpoint will require a USER role
                // - Notice how the base endpoint (Ex. just /jobs) needs
                //   to be explicitly added here
                //
                // All users (regular users and admins) have role USER by
                //   default (based on my signup implementation)
                //
                // Here, we can also specify HttpMethod.GET, etc., but I won't
                //   specify HttpMethod here since I'll apply this rule to any
                //   method just to keep things simple
                //
                // There's also:  .hasAnyRole("USER", "ADMIN")
                //
                // Note that hasRole/hasAnyRole implies that the request must
                //   be authenticated
                .requestMatchers(
                    // HttpMethod.GET,
                    "/jobs",
                    "/jobs/**",
                    "/members",
                    "/members/**"
                ).hasRole("USER")

                // Require any other request that doesn't match anything above
                //   to be authenticated
                //
                // There's also:  .anyRequest().denyAll()
                // - This one rejects any request not explicitly allowed
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(
                // (oauth2) -> oauth2.jwt(Customizer.withDefaults())
                (oauth2) -> oauth2.jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            );

        return http.build();
    }

    // Used below for the jwtDecoder
    //
    // ***** How does jwtDecoder know that it should use this bean instead of
    //   another SecretKey bean?
    // - Spring first looks for a bean matching the parameter's type (in my
    //   case, SecretKey). If there's only one matching bean, Spring doesn't
    //   require that parameter's name to match the bean's name
    // - But what if there's multiple beans of the same type?
    //   -- OPTION 1: @Qualifier("secretKey") SecretKey secretKey, where
    //      "secretKey" is the bean's name
    //   -- OPTION 2: Just have the parameter's name match the bean's name
    //
    // IMPORTANT: In application.properties, I'll need:
    //     jwt.secret=${JWT_SECRET}
    // - JWT_SECRET is an environment variable
    // - Note that @Value doesn't just read from application.properties.
    //   Instead, it reads from Spring's Environment which combines multiple
    //   property sources including application.properties, OS environment
    //   variables, command-line arguments, etc.
    //
    // Java's SecretKeySpec constructor doesn't enforce the HS256 minimum key
    //   length, which is why we have the keyBytes length check below
    @Bean
    public SecretKey jwtSecretKey(
        @Value("${jwt.secret}") String secretString
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secretString);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret must be at least 256 bits"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // I'll be using HS256 instead of RS256 to keep things simple.
    // - NOTE: HS256 is HMAC-SHA256, which is a symmetric signing algorithm used
    //   JWTs that combine a hash-based message authentication code (HMAC) with
    //   the SHA-256 hash function
    //   -- Basically, it uses a single shared secret key for both
    //      generating/signing and validating the token
    // - I can just generate a cryptographically random secret with at least
    //   256 bits, which only the backend app should use (KEEP IT
    //   OUTSIDE OF SOURCE CODE !!! + don't give it to the frontend code)
    //   -- To generate one, use this in terminal:    openssl rand -base64 32
    // 
    // For NimbusJwtDecoder.withSecretKey(), Spring Security 6.5 defaults
    //   to HS256.
    // - No need to add    .macAlgorithm(MacAlgorithm.HS256)
    //   ...which tells NimbusJwtDecoder which HMAC signing algorithm to
    //   trust when verifying incoming JWTs
    // - Note that Spring Security docs use .jwsAlgorithm(), which is used for
    //   a JWKS-based decoder
    //
    // For now, I won't be adding an iss claim to my JWTs so I won't have this
    //   line that Spring Security docs keep showing:
    //      jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(this.issuer));
    // - Therefore, I won't have issuer validation. But I'll still have
    //   signature verification, which simply confirms that the JWT has been
    //   signed with the secret key
    //
    // SecretKey vs SecretKeySpec
    // - Neither are provided by Spring Security. They come from javax.crypto
    // - SecretKey is an interface representing symmetric cryptographic keys
    // - SecretKeySpec is a concrete implementation that represents a secret key
    //   using raw bytes and an algorithm name
    //   -- SecretKeySpec creates a key object from existing bytes, such as a
    //      base64-decoded environment variable.
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
            .withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    // IMPORTANT: This NimbusJwtEncoder builder is only available starting
    //   Spring Security 7
    // This is simply a JwtEncoder that uses the same secret key as the
    //   decoder to sign tokens
    // https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/JwtEncoder.html
    // https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/NimbusJwtEncoder.html
    // https://docs.spring.io/spring-security/reference/api/java/org/springframework/security/oauth2/jwt/NimbusJwtEncoder.SecretKeyJwtEncoderBuilder.html
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder
            .withSecretKey(jwtSecretKey)
            .algorithm(MacAlgorithm.HS256)
            .build();
    }
}

// Source for disabling CSRF protection:
// - https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#disable-csrf
//
// Source for authorizeHttpRequests
// - https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
// - Source for Matching using Ant
//   -- https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html#match-by-ant
//
// Source for SessionCreationPolicy.STATELESS
// - https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html
//
// Source for OAuth 2.0 Resource Server JWT
// - https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html

/*
------------------------- REQUEST AUTHENTICATION EXPLANATION -------------------

***** NOTE: Refer to auth/AuthService.java file for some explanations of other
  components such as AuthenticationManager, ProviderManager, etc.

HTTP Request
    |
SecurityFilterChain
    |
BearerTokenAuthenticationFilter
- https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html
  -- When the user submits their bearer token, the
     BearerTokenAuthenticationFilter creates a BearerTokenAuthenticationToken
     which is a type of Authentication by extracting the token from the
     HttpServletRequest.
    |
***** NOTE: This is where a lot of steps mirror those in the login flow
    |
BearerTokenAuthenticationToken (unauthenticated. Again, this is an Authentication object)
- Synonymous to UsernamePasswordAuthenticationToken in the login flow
- The filter simply uses AuthenticationManager's authenticate method like in
  the login flow
    |
AuthenticationManager
    |
ProviderManager
    |
JwtAuthenticationProvider
- Synonymous to DaoAuthenticationProvider in the login flow
- https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html
  -- ME: This page actually shows the flow shown below + some explanations
  1. JwtAuthenticationProvider decodes, verifies, and validates the Jwt using
     a JwtDecoder
  2. JwtAuthenticationProvider then uses the JwtAuthenticationConverter to
     convert the Jwt into a Collection of granted authorities
  3. When authentication is successful, the Authentication that is returned is
     of type JwtAuthenticationToken and has a principal that is the Jwt
     returned by the configured JwtDecoder.
     Ultimately, the returned JwtAuthenticationToken will be set on the
     SecurityContextHolder by the authentication Filter.
    |
JwtDecoder
- Synonymous to PasswordEncoder in the login flow
- What this does:
  -- parses JWT
  -- verifies signature
  -- validates token (expiration, etc.)
    |
Jwt (contains decoded token + claims)
    |
JwtAuthenticationConverter
- Determines authorities and creates the Authentication
    |
JwtAuthenticationToken (authenticated)
    |
JwtAuthenticationProvider -> ProviderManager -> BearerTokenAuthenticationFilter
    |
***** NOTE: This is where the steps are once again different from the login flow  
    |
SecurityContextHolder (put the JwtAuthentication into this)
    |
AuthorizationFilter
    |
Controller


***** I DON'T NEED TO IMPLEMENT THESE
What are issuer-uri, jwk-set-uri, withIssuerLocation(), audiences, etc.?
- They're simply ways for your resource server to discover the public
    keys needed to verify JWTs
- This is used when using a separate authentication provider instead of
    having your Spring Boot app generate the JWTs
- iss claim in JWT
    -- Don't need this
- JWK (JSON Web Key): JSON representation of a cryptographic key
    -- Don't need this
- .withIssuerLocation(): Same idea as setting issuer-uri
- audiences
  -- "Boot also has the audiences property for validating the aud claim;
     this is who the JWT was sent to"
*/