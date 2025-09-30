package com.example.microsponsoringbackend.security;

import com.example.microsponsoringbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    // Préfixes publics généraux (toujours autorisés)
    private static final String[] PUBLIC_PREFIXES = new String[] {
        "/api/auth/",          // login, register, reset...
        "/api/public",
        "/api/health",
        "/actuator",
        "/api/images/",
        "/ws-notifications"
    };

    // Endpoints exacts publics
    private static final String[] PUBLIC_EXACT = new String[] {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/validate-reset-token"
    };

    private boolean isPublic(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final String method = request.getMethod();

        // 1) Preflight CORS
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // 2) Exacts
        for (String exact : PUBLIC_EXACT) if (uri.equals(exact)) return true;

        // 3) Préfixes
        for (String p : PUBLIC_PREFIXES) if (uri.startsWith(p)) return true;

        // 4) Groupes publics en GET uniquement
        if ("GET".equalsIgnoreCase(method)) {
            if (uri.startsWith("/api/companies-non-profits")) return true;
            if (uri.startsWith("/api/recognition-benefits/company/")) return true; // <-- CORRECT
            // Exemple si tu rends public : GET /api/users/{id}/organisation-profile
            // if (uri.matches("^/api/users/[^/]+/organisation-profile$")) return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        logger.info("JWT Filter processing request: {}", requestURI);

        // Laisser passer les routes publiques sans vérifier le JWT
        if (isPublic(request)) {
            logger.info("Skipping JWT check for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Récupération de l'en-tête Authorization
        final String header = request.getHeader("Authorization");
        if (header == null) {
            logger.warn("No Authorization header found for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ")) {
            logger.warn("Authorization header does not start with Bearer on URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);

        try {
            final String username = jwtUtil.extractUsername(token);
            logger.info("Extracted username from token for URI {}: {}", requestURI, username);

            boolean noAuthOrAnonymous =
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken;

            if (username != null && noAuthOrAnonymous) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("Loaded user details for {} with authorities: {}", username, userDetails.getAuthorities());

                if (!jwtUtil.validateToken(token, userDetails)) {
                    logger.warn("Invalid JWT token for user: {} on URI: {}", username, requestURI);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }

                Collection<? extends GrantedAuthority> tokenAuthorities = jwtUtil.extractAuthorities(token);
                Collection<? extends GrantedAuthority> authorities =
                        (tokenAuthorities == null || tokenAuthorities.isEmpty())
                                ? userDetails.getAuthorities()
                                : tokenAuthorities;

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("JWT authentication successful for user: {} on URI: {}", username, requestURI);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("JWT authentication error on {}: {}", requestURI, e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
        }
    }
}
