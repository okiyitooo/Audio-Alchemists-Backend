package com.kanaetochi.audio_alchemists.controller;

import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.security.UserDetailsImpl;
import com.kanaetochi.audio_alchemists.service.FollowService;
import com.kanaetochi.audio_alchemists.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users/follow")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FollowController {

    private final UserService userService;
    final private FollowService followService;

    @PostMapping("/{userIdToFollow}")
    public ResponseEntity<Void> follow(@PathVariable Long userIdToFollow, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        User user = userService.getUserById(currentUser.getId()).get();
        if (user == null) return ResponseEntity.notFound().build();
        followService.followUser(user, userIdToFollow);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userIdToUnfollow}")
    public ResponseEntity<Void> unfollow(@PathVariable Long userIdToUnfollow, @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (currentUser == null) return ResponseEntity.status(401).build();
        User user = userService.getUserById(currentUser.getId()).get();
        if (user == null) return ResponseEntity.notFound().build();
        followService.unfollowUser(user, userIdToUnfollow);
        return ResponseEntity.noContent().build();
    }

    // Endpoint to get users someone is following
    @GetMapping("/{userId}/following")
    public ResponseEntity<Set<Long>> getFollowing(@PathVariable Long userId) {
         Set<Long> followingIds = followService.getFollowingIds(userId);
         return ResponseEntity.ok(followingIds);
    }

     // Endpoint to get a users followers
     @GetMapping("/{userId}/followers")
     public ResponseEntity<Set<Long>> getFollowers(@PathVariable Long userId) {
         Set<Long> followerIds = followService.getFollowerIds(userId); // Needs repository method
         return ResponseEntity.ok(followerIds);
     }

     // Endpoint to check if current user follows another user
     @GetMapping("/check/{userIdToCheck}")
     public ResponseEntity<Boolean> checkFollowing(@PathVariable Long userIdToCheck, @AuthenticationPrincipal UserDetailsImpl currentUser) {
          if (currentUser == null) return ResponseEntity.status(401).build();
          boolean isFollowing = followService.isFollowing(currentUser.getId(), userIdToCheck);
          return ResponseEntity.ok(isFollowing);
     }
}