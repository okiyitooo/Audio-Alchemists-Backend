package com.kanaetochi.audio_alchemists.service;

import java.util.List;

import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.ProjectVersion;
import com.kanaetochi.audio_alchemists.model.User;

public interface ProjectVersionService {
    ProjectVersion createSnapShot(Project project, User savedBy, String description);
    List<ProjectVersion> getVersionsForProject(Long projectId);
    Project revertToVersion(Long projectId, Long versionId, User requestedBy); // pass user for audit
}
