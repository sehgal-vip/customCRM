package com.turno.crm.security;

import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.UserStatus;
import com.turno.crm.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository,
                                   TokenBlacklist tokenBlacklist) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/auth/users") ||
               path.equals("/api/v1/auth/dev-login") ||
               path.startsWith("/api/v1/webhook/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            if (tokenBlacklist.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
            } else if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                Optional<User> optionalUser = userRepository.findById(userId);

                if (optionalUser.isPresent() && optionalUser.get().getStatus() == UserStatus.ACTIVE) {
                    User user = optionalUser.get();
                    String authority = "ROLE_" + user.getRole().name();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority(authority))
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
