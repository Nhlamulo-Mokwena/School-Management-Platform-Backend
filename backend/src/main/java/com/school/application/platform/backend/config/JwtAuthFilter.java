package com.school.application.platform.backend.config;

import com.school.application.platform.backend.service.JwtUtilService;
import com.school.application.platform.backend.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtilService jwtUtilService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtUtilService jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtilService = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Step 1: Pull the Authorization header from the request
        // It should look like: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            // Step 2: Strip the "Bearer " prefix to get just the token string
            String token = header.substring(7);

            // Step 3: Only proceed if the token is valid (not expired, not tampered)
            if (jwtUtilService.validateToken(token)) {
                // Step 4: Get the email from the token, then load the full user from DB
                String email = jwtUtilService.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 5: Create an authentication object containing the user and their role.
                // The null in the middle is the credentials (password) — we don't need
                // it here since the JWT itself already proved who this user is.
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 6: Store the authentication in the SecurityContext.
                // This is how Spring knows for the rest of this request who is logged in
                // and what role they have — controllers can read this later.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Always continue down the filter chain, even if no token was found.
        // Unauthenticated requests to protected endpoints will be rejected by
        // SecurityConfig's rules, not here.
        chain.doFilter(request, response);
    }
}