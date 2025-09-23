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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("JWT Filter processing request: {}", requestURI);
        
        // Skip JWT check for public endpoints that don't require authentication
        if (requestURI.equals("/api/auth/login") || 
            requestURI.equals("/api/auth/register") ||
            requestURI.equals("/api/auth/forgot-password") ||
            requestURI.equals("/api/auth/reset-password") ||
            requestURI.equals("/api/auth/validate-reset-token") ||
            requestURI.startsWith("/api/users") ||
            requestURI.startsWith("/api/images") ||
            (requestURI.startsWith("/api/companies-non-profits") && request.getMethod().equals("GET")) ||
            requestURI.startsWith("/api/recognition-benefits/company/**") ||
            requestURI.startsWith("/api/public") ||
            requestURI.startsWith("/api/health") ||
            requestURI.startsWith("/actuator")) {
            logger.info("Skipping JWT check for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header: " + header);
        logger.info("Processing request: {} with Authorization header: {}", requestURI, header);
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                logger.info("Extracted username from token: {}", username);
                
                if (username != null &&
                    (SecurityContextHolder.getContext().getAuthentication() == null ||
                     SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("Loaded user details for {} with authorities: {}", username, userDetails.getAuthorities());
                    
                    if (jwtUtil.validateToken(token, userDetails)) {
                        // Extract authorities from JWT token and ensure they have ROLE_ prefix
                        Collection<? extends GrantedAuthority> tokenAuthorities = jwtUtil.extractAuthorities(token);
                        Collection<? extends GrantedAuthority> authorities = tokenAuthorities.isEmpty() ? 
                            userDetails.getAuthorities() : tokenAuthorities;
                        
                        logger.info("Using authorities for authentication: {}", authorities);
                        
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("JWT authentication successful for user: {} on URI: {}", username, requestURI);
                    } else {
                        logger.warn("Invalid JWT token for user: {} on URI: {}", username, requestURI);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("JWT authentication error: {} on URI: {}", e.getMessage(), requestURI);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            }
        } else if (header != null) {
            logger.warn("Authorization header does not start with Bearer: {} on URI: {}", header, requestURI);
        } else {
            logger.warn("No Authorization header found for URI: {}", requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
}