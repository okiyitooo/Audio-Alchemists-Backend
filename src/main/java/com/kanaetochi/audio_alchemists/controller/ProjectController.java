package com.kanaetochi.audio_alchemists.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.service.ProjectService;

import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project projectDetails, @RequestParam Long id) {
        Project project = projectService.createProject(projectDetails, id);
        URI uri = URI.create("/projects/" + project.getId());
        return ResponseEntity.created(uri).body(project);
    }
    @GetMapping
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@RequestParam Long id) {
        return projectService.getProjectById(id)
                .map(project -> ResponseEntity.ok().body(project))
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@RequestParam Long id, @RequestBody Project projectDetails) {
        Project project = projectService.updateProject(id, projectDetails);
        return ResponseEntity.ok().body(project);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@RequestParam Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}
