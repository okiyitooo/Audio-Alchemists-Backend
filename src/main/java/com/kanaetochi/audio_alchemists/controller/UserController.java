package com.kanaetochi.audio_alchemists.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.dto.UserDto;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(user -> modelMapper.map(user, UserDto.class)).toList();
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") long userId){
       return userService.getUserById(userId)
               .map(user -> new ResponseEntity<>(modelMapper.map(user, UserDto.class), HttpStatus.OK))
               .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") long id, @RequestBody User userDetails){
        User updatedUser = userService.updateUser(id, userDetails);
        UserDto userDto = modelMapper.map(updatedUser, UserDto.class);
        return  new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@AuthenticationPrincipal User authenticatedUser, @RequestBody User userDetails){
        if (authenticatedUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } 
        // Only allow updating the role if the authenticated user is an admin
        if (userDetails.getRole() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User updatedUser = userService.updateUser(authenticatedUser.getId(), userDetails);
        UserDto userDto = modelMapper.map(updatedUser, UserDto.class);
        return  new ResponseEntity<>(userDto, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id){
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>("User successfully deleted!", HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }
}
