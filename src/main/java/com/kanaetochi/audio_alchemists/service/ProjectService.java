package com.kanaetochi.audio_alchemists.service;

import java.util.List;
import java.util.Optional;

import com.kanaetochi.audio_alchemists.dto.ProjectVersionDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;

public interface ProjectService {
    Project createProject(Project project, Long userId);
    List<Project> getAllProjects();
    Optional<Project> getProjectById(Long id);
    Project updateProject(Long id, Project projectDetails);
    void deleteProject(Long id);
    ProjectVersionDto saveNewVersion(Long projectId, String description, User savedBy);
    List<Project> searchProjects(String query);
}
