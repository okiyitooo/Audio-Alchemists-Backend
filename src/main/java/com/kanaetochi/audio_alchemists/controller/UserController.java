package com.kanaetochi.audio_alchemists.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long userId){
       return userService.getUserById(userId)
               .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
               .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") long id, @RequestBody User userDetails){
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return  new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
