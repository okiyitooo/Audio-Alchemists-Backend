package com.kanaetochi.audio_alchemists.service;

import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User registerUser(RegisterDto user);
    List<User> getAllUsers();
    Optional<User> getUserById(long id);
    User updateUser(long id, User userDetails);
    void deleteUser(long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}