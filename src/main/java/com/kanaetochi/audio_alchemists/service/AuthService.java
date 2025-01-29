package com.kanaetochi.audio_alchemists.service;

import org.springframework.security.core.Authentication;

import com.kanaetochi.audio_alchemists.dto.LoginDto;

public interface AuthService {
    
    Authentication authenticateUser(LoginDto loginDto);
    String generateToken(Authentication authentication);
}
