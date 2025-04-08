package com.kanaetochi.audio_alchemists.service.impl;

// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanaetochi.audio_alchemists.service.ProjectService;
import com.kanaetochi.audio_alchemists.service.ProjectVersionService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
// import com.kanaetochi.audio_alchemists.security.UserDetailsImpl;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
	private final ProjectVersionService projectVersionService;

	private static final ThreadLocal<Boolean> isReverting = ThreadLocal.withInitial(() -> false);
	@Override
	public List<Project> getAllProjects() {
		return projectRepository.findAll();
	}

	@Override
	public Project createProject(Project project, Long userId) {
		User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + userId));
        project.setOwner(user);
        return projectRepository.save(project);
	}

	@Override
	@Transactional
	public Project updateProject(Long id, Project project) {
		Project project2 = projectRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Project not found with id: " + id));
        project2.setTitle(project.getTitle());
        project2.setDescription(project.getDescription());
        project2.setGenre(project.getGenre());
        project2.setTempo(project.getTempo());
        Project updatedProject = projectRepository.save(project2);
		// if(!isReverting.get()) {
		// 	User currentUser = getCurrentUser();
		// 	projectVersionService.createSnapShot(updatedProject, currentUser, "Project updated");
		// }
		return updatedProject;
	}

	public static void setReverting(boolean reverting){
		isReverting.set(reverting);
	}

	// private User getCurrentUser() {
	// 	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	// 	if(authentication != null && (authentication.getPrincipal() instanceof UserDetailsImpl)) {
	// 		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	// 		return userRepository.findById(userDetails.getId()).orElseThrow(()-> new ResourceNotFoundException("User", "id",userDetails.getId()));
	// 	}
	// 	throw new RuntimeException("No authenticated user found");
	// }

	@Override
	public Optional<Project> getProjectById(Long id) {
		return projectRepository.findById(id);
	}

	@Override
	@Transactional
	public void deleteProject(Long id) {
		projectRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.deleteById(id);
	}

	public static void executeAndRevert(Runnable action) {
		try {
			isReverting.set(true);
			action.run();
		} finally {
			isReverting.remove();
		}
	}

	@Override
	@Transactional
	public void saveNewVersion(Long projectId, String description, User savedBy) {
		Project project = projectRepository.findById(projectId).orElseThrow(()-> new ResourceNotFoundException("Project", "id", projectId));

		projectVersionService.createSnapShot(project, savedBy, description);
		log.info("Explicily saved new version for project ID: {} by user: {}", projectId, savedBy != null ? savedBy.getUsername() : "system");
	}

	@Override
	@Transactional(readOnly = true) // Search is read-only
	public List<Project> searchProjects(String query) {
		log.info("Searching projects with query: {}", query);
		if (query == null || query.trim().isEmpty()) {
			return Collections.emptyList();
		}
		// Basic sanitization to prevent SQL injection or other issues
		String sanitizedQuery = query.trim();
		return projectRepository.searchProjects(sanitizedQuery);
	}
	
}
