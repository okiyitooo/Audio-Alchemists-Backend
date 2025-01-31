package com.kanaetochi.audio_alchemists.service.impl;

import com.kanaetochi.audio_alchemists.service.UserService;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository=userRepository;
    }

	@Override
	public User updateUser(long id, User userDetails){
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id "+ id+" not found"));
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
		if(userDetails.getPassword()!=null)
			user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        user.setRole(userDetails.getRole());
        userRepository.save(user);
        return user;
	}

	@Override
	public void deleteUser(long id){
		userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id "+ id+" not found"));
        userRepository.deleteById(id);
	}

	@Override
	public Optional<User> getUserById(long id) {
		return userRepository.findById(id);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public User registerUser(RegisterDto registerDto) {
        User user = User.builder()
            .email(registerDto.getEmail())
            .password(registerDto.getPassword())
            .username(registerDto.getUsername())
            .role(Role.valueOf(registerDto.getRole()))
            .build();
        ;
		user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public boolean existsByUsername(String username) {
		return userRepository.existsByUsername(username);
	}
}
