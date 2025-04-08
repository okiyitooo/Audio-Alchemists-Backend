package com.kanaetochi.audio_alchemists.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.dto.RecommendedProjectDto;
import com.kanaetochi.audio_alchemists.dto.RecommendedUserDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.*; // Import all needed repositories
import com.kanaetochi.audio_alchemists.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CollaborationRepository collaborationRepository;
    private final FollowRepository followRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedProjectDto> getProjectRecommendations(User user, int limit) {
        log.info("Generating project recommendations for user ID: {}", user.getId());
        Set<RecommendedProjectDto> recommendations = new LinkedHashSet<>();
        Pageable pageable = PageRequest.of(0, limit * 2); // Fetch more initially to allow filtering

        // 1. Get projects user owns or collaborates on to exclude them
        List<Long> collaboratedProjectIds = collaborationRepository.findProjectIdsByUserId(user.getId());
        List<Long> ownedProjectIds = projectRepository.findIdsByOwnerId(user.getId());
        Set<Long> excludedProjectIds = new HashSet<>(collaboratedProjectIds);
        excludedProjectIds.addAll(ownedProjectIds);
        excludedProjectIds.add(-1L); // Add dummy ID in case the list is empty for IN clause

        // 2. Genre-Based Recommendations (Example: Assuming preferences is a simple list in JSON)
         List<String> preferredGenres = extractPreferredGenres(user); // Implement this helper
         if (!preferredGenres.isEmpty()) {
             List<Project> genreProjects = projectRepository.findProjectsByGenreAndExclude(
                 preferredGenres, new ArrayList<>(excludedProjectIds), user.getId(), pageable);
             genreProjects.forEach(p -> recommendations.add(mapToRecommendedProjectDto(p, "Matches your preferred genre: " + p.getGenre())));
             if (recommendations.size() >= limit) return new ArrayList<>(recommendations).subList(0, limit);
         }

        // 3. Following-Based Recommendations
        Set<Long> followingIds = followRepository.findFollowingIdsByFollowerId(user.getId());
        if (!followingIds.isEmpty()) {
             // Update excluded IDs based on current recommendations size
             excludedProjectIds.addAll(recommendations.stream().map(RecommendedProjectDto::getId).collect(Collectors.toSet()));
             List<Project> followedProjects = projectRepository.findProjectsByOwnerIdsAndExclude(
                 new ArrayList<>(followingIds), new ArrayList<>(excludedProjectIds), pageable);
             followedProjects.forEach(p -> recommendations.add(mapToRecommendedProjectDto(p, "From user you follow: " + p.getOwner().getUsername())));
             if (recommendations.size() >= limit) return new ArrayList<>(recommendations).subList(0, limit);
        }

        // Add more strategies here (e.g., based on common collaborators) if needed

        log.info("Generated {} project recommendations for user ID: {}", Math.min(recommendations.size(), limit), user.getId());
        return new ArrayList<>(recommendations).stream().limit(limit).collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<RecommendedUserDto> getUserRecommendations(User user, int limit) {
         log.info("Generating user recommendations for user ID: {}", user.getId());
         Set<RecommendedUserDto> recommendations = new LinkedHashSet<>();
         Pageable pageable = PageRequest.of(0, limit * 2);

         Set<Long> alreadyFollowing = followRepository.findFollowingIdsByFollowerId(user.getId());
         Set<Long> excludeUserIds = new HashSet<>(alreadyFollowing);
         excludeUserIds.add(user.getId()); // Exclude self
         excludeUserIds.add(-1L); // Dummy

         // 1. Shared Collaborators
         List<Long> projectIdsUserCollabsOn = collaborationRepository.findProjectIdsByUserId(user.getId());
         if (!projectIdsUserCollabsOn.isEmpty()) {
             List<Long> sharedCollaboratorIds = collaborationRepository.findCollaboratorIdsByProjectIdsExcludingUserId(projectIdsUserCollabsOn, user.getId());
             // Fetch users not already followed
             List<User> sharedCollaborators = userRepository.findByIdInAndIdNotIn(sharedCollaboratorIds, excludeUserIds, pageable);
             sharedCollaborators.forEach(u -> recommendations.add(mapToRecommendedUserDto(u, "Collaborates on similar projects")));
             if (recommendations.size() >= limit) return new ArrayList<>(recommendations).subList(0, limit);
         }

         // 2. Follows of Follows ("Friends of Friends")
         if (!alreadyFollowing.isEmpty()) {
             // Update excluded IDs
             excludeUserIds.addAll(recommendations.stream().map(RecommendedUserDto::getId).collect(Collectors.toSet()));
             Set<Long> followsOfFollowsIds = followRepository.findFollowingIdsByFollowerIds(alreadyFollowing);
             List<User> followsOfFollows = userRepository.findByIdInAndIdNotIn(new ArrayList<>(followsOfFollowsIds), excludeUserIds, pageable);
             followsOfFollows.forEach(u -> recommendations.add(mapToRecommendedUserDto(u, "Followed by people you follow")));
             if (recommendations.size() >= limit) return new ArrayList<>(recommendations).subList(0, limit);
         }

         /*
            Add more strategies here if needed, such as:
            - Users with similar project genres
         */

         log.info("Generated {} user recommendations for user ID: {}", Math.min(recommendations.size(), limit), user.getId());
         return new ArrayList<>(recommendations).stream().limit(limit).collect(Collectors.toList());
    }


     private List<String> extractPreferredGenres(User user) {
         if (user.getPreferences() == null) {
             return Collections.emptyList();
         }
         try {
             // preferences JSON looks like: {"favoriteGenres": ["Rock", "Jazz"]}
             Map<String, List<String>> prefs = objectMapper.readValue(
                user.getPreferences(), 
                new TypeReference<Map<String, List<String>>>() {}
            ); 
             return prefs.getOrDefault("favoriteGenres", Collections.emptyList());
         } catch (Exception e) {
             log.warn("Could not parse preferences JSON for user {}: {}", user.getId(), e.getMessage());
             return Collections.emptyList();
         }
     }

     private RecommendedProjectDto mapToRecommendedProjectDto(Project project, String reason) {
         RecommendedProjectDto dto = modelMapper.map(project, RecommendedProjectDto.class);
         if (project.getOwner() != null) {
             dto.setOwnerUsername(project.getOwner().getUsername());
         }
         dto.setReason(reason);
         return dto;
     }

      private RecommendedUserDto mapToRecommendedUserDto(User user, String reason) {
         RecommendedUserDto dto = modelMapper.map(user, RecommendedUserDto.class);
         dto.setReason(reason);
         return dto;
     }
}