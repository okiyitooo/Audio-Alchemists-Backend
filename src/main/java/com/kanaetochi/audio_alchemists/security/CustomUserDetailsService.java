package com.kanaetochi.audio_alchemists.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{
    private UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + username));
    }

    public UserDetails loadUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->new UsernameNotFoundException("User not found with id : " + id));
    }
}
