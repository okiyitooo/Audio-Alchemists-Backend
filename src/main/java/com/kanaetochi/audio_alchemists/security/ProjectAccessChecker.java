package com.kanaetochi.audio_alchemists.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.service.CollaborationService;

import lombok.RequiredArgsConstructor;

@Component("projectAccessChecker")
@RequiredArgsConstructor
public class ProjectAccessChecker {
    private final ProjectRepository projectRepository;
    final private CollaborationService collaborationService;

    public boolean canViewProject(Authentication authentication, Long projectId) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null) {
            return false;
        }

        Optional <Project> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return false;
        }
        Project projectDetails = project.get();
        if (projectDetails.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }
        
        return collaborationService.isUserCollaborator(projectId, currentUser.getId());
    }
    public boolean canEditProject(Authentication authentication, Long projectId) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null) {
            return false;
        }
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return false;
        }
        Project project = projectOpt.get();
        if (project.getOwner().getId().equals(currentUser.getId())) {
            return true;
        }
        return collaborationService.getCollaboratorsByProject(projectId)
                .stream()
                .anyMatch(collab -> collab.getUser().getId().equals(currentUser.getId()) 
                        && collab.getRole().equalsIgnoreCase("EDITOR"));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User userDetails) {
            return userDetails;
        }
        return null;
    }
}
