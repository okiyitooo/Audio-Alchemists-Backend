package com.kanaetochi.audio_alchemists.service.impl;

import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.service.ProjectService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
	@Override
	public List<Project> getAllProjects() {
		return projectRepository.findAll();
	}

	@Override
	public Project createProject(Project project, Long id) {
		User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + id));
        project.setOwner(user);
        return projectRepository.save(project);
	}

	@Override
	public Project updateProject(Long id, Project project) {
		Project project2 = projectRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Project not found with id: " + id));
        project2.setTitle(project.getTitle());
        project2.setDescription(project.getDescription());
        project2.setGenre(project.getGenre());
        project2.setTempo(project.getTempo());
        return projectRepository.save(project2);
	}

	@Override
	public Optional<Project> getProjectById(Long id) {
		return projectRepository.findById(id);
	}

	@Override
	public void deleteProject(Long id) {
		projectRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.deleteById(id);
	}
}
