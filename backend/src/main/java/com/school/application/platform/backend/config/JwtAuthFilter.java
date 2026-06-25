package com.school.application.platform.backend.config;

import com.school.application.platform.backend.service.JwtUtilService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtilService jwtUtilService;

    public JwtAuthFilter(JwtUtilService jwtUtilService) {
        this.jwtUtilService = jwtUtilService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtilService.validateToken(token)) {
                String email = jwtUtilService.getEmailFromToken(token);

                // Read roles directly from the token — no database call needed.
                // The token already contains ["ROLE_PARENT"] or ["ROLE_ADMIN"]
                // because we embedded them when generating it on login.
                List<String> roles = jwtUtilService.getRolesFromToken(token);

                // Explicitly typed as List<SimpleGrantedAuthority> so the
                // UsernamePasswordAuthenticationToken constructor accepts it
                // as Collection<? extends GrantedAuthority> without complaint.
                List<SimpleGrantedAuthority> authorities = roles == null
                        ? List.of()
                        : roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                var auth = new UsernamePasswordAuthenticationToken(
                        email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}