package com.kanaetochi.audio_alchemists.service.impl;

import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Collaboration;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.CollaborationRepository;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
import com.kanaetochi.audio_alchemists.service.CollaborationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {
    private final CollaborationRepository collaborationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public Collaboration addCollaborator(Long projectId, Long userId, String role) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (collaborationRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Collaboration", "project and user", project.getId() + " and " + user.getId());
        }
        Collaboration collaboration = Collaboration.builder()
                .project(project)
                .user(user)
                .role(role)
                .build();
        return collaborationRepository.save(collaboration);
    }

    @Override
    public void removeCollaborator(Long projectId, Long userId) {
        Collaboration collaboration = collaborationRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration", "projectId", projectId + " and UserId " + userId));
        collaborationRepository.delete(collaboration);
    }

    @Override
    public List<Collaboration> getCollaboratorsByProject(Long projectId) {
        return collaborationRepository.findByProjectId(projectId);
    }

    @Override
    public boolean isUserCollaborator(Long projectId, Long userId) {
        return collaborationRepository.existsByProjectIdAndUserId(projectId, userId);
    }
}
