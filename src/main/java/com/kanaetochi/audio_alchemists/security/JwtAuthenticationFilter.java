package com.kanaetochi.audio_alchemists.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter( CustomUserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider) {
        this.tokenProvider = jwtTokenProvider;
        this.customUserDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Long userId = tokenProvider.getUserIdFromJWT(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentiacation = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentiacation.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentiacation);
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
}
