package com.kanaetochi.audio_alchemists.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.security.JwtTokenProvider;
import com.kanaetochi.audio_alchemists.service.AuthService;


@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Authentication authenticateUser(LoginDto loginDto) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));
    }

    @Override
    public String generateToken(Authentication authentication) {
        return tokenProvider.generateToken(authentication);
    }
}
