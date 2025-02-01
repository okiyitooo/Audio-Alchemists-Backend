package com.kanaetochi.audio_alchemists.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.dto.ProjectDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.service.ProjectService;

import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody Project projectDetails, @AuthenticationPrincipal User autheticatedUser) {
        if (autheticatedUser == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = autheticatedUser.getId();
        Project project = projectService.createProject(projectDetails, userId);
        URI uri = URI.create("/projects/" + project.getId());
        return ResponseEntity.created(uri).body(modelMapper.map(project, ProjectDto.class));
    }
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects().stream().map(project -> modelMapper.map(project, ProjectDto.class)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        return  projectService.getProjectById(id)
               .map(project -> {
                   ProjectDto projectDto = modelMapper.map(project, ProjectDto.class);
                   return new ResponseEntity<>(projectDto, HttpStatus.OK);
               })
               .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        Project project = projectService.updateProject(id, projectDetails);
        return ResponseEntity.ok().body(modelMapper.map(project, ProjectDto.class));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().body("Project deleted successfully");
    }
}
