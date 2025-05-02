package com.kanaetochi.audio_alchemists.controller;

import com.kanaetochi.audio_alchemists.dto.RecommendedProjectDto;
import com.kanaetochi.audio_alchemists.dto.RecommendedUserDto;
import com.kanaetochi.audio_alchemists.model.User; // Import User
import com.kanaetochi.audio_alchemists.service.RecommendationService;
import com.kanaetochi.audio_alchemists.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Must be logged in for recommendations
public class RecommendationController {

    private final RecommendationService recommendationService;
    final private UserService userService;

    @GetMapping("/projects")
    public ResponseEntity<List<RecommendedProjectDto>> getProjectRecommendations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "5") int limit) {

        if (currentUser == null) return ResponseEntity.status(401).build();

        User user = userService.getUserById(currentUser.getId()).get();

        if (user == null) return ResponseEntity.status(401).build();
        
        List<RecommendedProjectDto> recommendations = recommendationService.getProjectRecommendations(
                user, 
                Math.max(1, Math.min(limit, 20)) // Clamp limit
        );
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/users")
    public ResponseEntity<List<RecommendedUserDto>> getUserRecommendations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "5") int limit) {

        if (currentUser == null) return ResponseEntity.status(401).build();

        User user = userService.getUserById(currentUser.getId()).get();

        if (user == null) return ResponseEntity.status(401).build();

        List<RecommendedUserDto> recommendations = recommendationService.getUserRecommendations(
                user,
                 Math.max(1, Math.min(limit, 20))
        );
        return ResponseEntity.ok(recommendations);
    }
}