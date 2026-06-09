package com.school.application.platform.backend.config;

import com.school.application.platform.backend.config.JwtAuthFilter;
import com.school.application.platform.backend.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
// Enables @PreAuthorize on controller methods so you can lock down
// individual endpoints by role (see AuthController example below)
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless REST APIs that use tokens,
                // since CSRF attacks rely on browsers auto-sending session cookies.
                .csrf(csrf -> csrf.disable())

                // Stateless means Spring will never create an HTTP session.
                // Each request must carry its own JWT — nothing is remembered between requests.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // /api/auth/register and /api/auth/login don't need a token —
                        // these are how users get a token in the first place
                        .requestMatchers("/api/auth/**").permitAll()

                        // Every other endpoint requires a valid JWT.
                        // Role-level restrictions are handled per-method with @PreAuthorize.
                        .anyRequest().authenticated()
                )

                // Register our JWT filter to run before Spring's built-in login filter.
                // This ensures the token is read and the user is authenticated
                // before any access decisions are made.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager is what the login endpoint uses to verify
    // email + password. Spring wires this up from our UserDetailsService + PasswordEncoder.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // BCrypt is the industry standard for hashing passwords.
    // It's intentionally slow to make brute-force attacks impractical.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Connects Spring's authentication system to our UserDetailsService (loads users
    // from DB) and our PasswordEncoder (verifies BCrypt hashes on login).
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
