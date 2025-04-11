package com.kanaetochi.audio_alchemists.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.dto.JWTAuthResponse;
import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.service.AuthService;
import com.kanaetochi.audio_alchemists.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto registerDto) {

        if(userService.existsByUsername(registerDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }
        if(userService.existsByEmail(registerDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        userService.registerUser(registerDto);

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTAuthResponse> authenticateUser(@RequestBody LoginDto entity) {
        Authentication auth = authService.authenticateUser(entity);
        String token = authService.generateToken(auth);
        
        return ResponseEntity.ok(new JWTAuthResponse(token));
    }
    
}