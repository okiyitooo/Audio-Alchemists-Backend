package com.kanaetochi.audio_alchemists.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setup(){
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
    }

    @Test
    void testRegisterUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(user.getUsername());
        registerDto.setEmail(user.getEmail());
        registerDto.setPassword(user.getPassword());
        registerDto.setRole(user.getRole().name());
        User registeredUser = userService.registerUser(registerDto);
        assertNotNull(registeredUser);
        assertEquals(registeredUser.getUsername(), user.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }


    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(users.size(),1);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> retrievedUser = userService.getUserById(1L);
        assertTrue(retrievedUser.isPresent());
        assertEquals(retrievedUser.get().getUsername(), user.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserByIdNotFound(){
       when(userRepository.findById(1L)).thenReturn(Optional.empty());
       Optional<User> optionalUser = userService.getUserById(1L);
       assertTrue(optionalUser.isEmpty());
       verify(userRepository, times(1)).findById(1L);

    }

    @Test
    void testUpdateUser() {
        User userDetails = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .role(Role.COMPOSER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updatedUser = userService.updateUser(1L,userDetails);
        assertNotNull(updatedUser);
        assertEquals(updatedUser.getUsername(), userDetails.getUsername());
        assertEquals(updatedUser.getEmail(), userDetails.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    void testUpdateUserNotFound(){
        User userDetails = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .role(Role.COMPOSER)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L,userDetails));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }


    @Test
    void testDeleteUserNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testExistsByUsername(){
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.existsByUsername("testuser"));
        verify(userRepository, times(1)).existsByUsername("testuser");

    }
    @Test
    void testExistsByEmail(){
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }
}