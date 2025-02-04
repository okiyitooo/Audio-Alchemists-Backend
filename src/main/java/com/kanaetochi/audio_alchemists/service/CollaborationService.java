package com.kanaetochi.audio_alchemists.service;

import java.util.List;

import com.kanaetochi.audio_alchemists.model.Collaboration;

public interface CollaborationService {
    Collaboration addCollaborator(Long projectId, Long userId, String role);
    void removeCollaborator(Long projectId, Long userId);
    List<Collaboration> getCollaboratorsByProject(Long projectId);

     boolean isUserCollaborator(Long projectId, Long userId);
}
