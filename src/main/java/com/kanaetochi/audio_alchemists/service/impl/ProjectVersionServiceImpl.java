package com.kanaetochi.audio_alchemists.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.ProjectVersion;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.ProjectVersionRepository;
import com.kanaetochi.audio_alchemists.repository.TrackRepository;
import com.kanaetochi.audio_alchemists.service.ProjectVersionService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectVersionServiceImpl implements ProjectVersionService {

    private final ProjectVersionRepository projectVersionRepository;
    private final TrackRepository trackRepository;
    private final ProjectRepository projectRepository;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ProjectVersion createSnapShot(Project project, User savedBy, String description) {
        try 
        {
            // Create a new ProjectVersion instance
            String snapshotJson = objectMapper.writeValueAsString(project);
            ProjectVersion version = ProjectVersion.builder()
                .savedBy(savedBy)
                .project(project)
                .description(description)
                .snapshotData(snapshotJson)
                .build();
            log.info("Creating snapshot for project ID: {}", project.getId());
            return projectVersionRepository.save(version);
        } catch (JsonProcessingException e) {
            log.error("Error creating snapshot for project ID {}: {}", project.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to serialize project for snapshot", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectVersion> getVersionsForProject(Long projectId) {
        log.info("Fetching versions for project ID: {}", projectId);
        return projectVersionRepository.findByProjectIdOrderByTimeStampDesc(projectId);
    }

    @Override
    @Transactional
    public Project revertToVersion(Long projectId, Long versionId, User requestedBy) {
        log.info("Reverting project ID: {} to version ID: {} by user: {}", projectId, versionId, requestedBy != null ? requestedBy.getUsername() : "system");
        
        // Fetch the project to revert
        Project currentProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        // Fetch the version to revert to
        ProjectVersion version = projectVersionRepository.findById(versionId)
                .filter(pv -> pv.getProject().getId().equals(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project Version", "id", versionId));
        
        // Revert the project to this version's snapshot data
        try {
            Project snapshotProject = objectMapper.readValue(version.getSnapshotData(), Project.class);

            // Update the current project with the snapshot data
            currentProject.setTitle(snapshotProject.getTitle());
            currentProject.setDescription(snapshotProject.getDescription());
            currentProject.setGenre(snapshotProject.getGenre());
            currentProject.setTempo(snapshotProject.getTempo());

            // Reconcile tracks
            Map<Long, Track> existingTracks = currentProject.getTracks().stream()
                    .collect(Collectors.toMap(Track::getId, Function.identity()));
            List<Track> newTracks = snapshotProject.getTracks() != null ? snapshotProject.getTracks() : List.of();
            
            // 1. Delete tracks present in current but not in snapshot
            List<Track> tracksToDelete = currentProject.getTracks().stream()
                    .filter(track -> newTracks.stream().noneMatch(newTrack -> newTrack.getId()!= null && newTrack.getId().equals(track.getId())))
                    .collect(Collectors.toList());
            log.debug("Deleting tracks: {}", tracksToDelete.stream().map(Track::getId).collect(Collectors.toList()));
            currentProject.getTracks().removeAll(tracksToDelete);
            trackRepository.deleteAll(tracksToDelete);

            // 2. Update existing tracks and add new tracks
            for (Track newTrack : newTracks) {
                Track trackToUpdate;
                if (newTrack.getId() != null && existingTracks.containsKey(newTrack.getId())) {
                    // Update existing track
                    trackToUpdate = existingTracks.get(newTrack.getId());
                    log.debug("Updating existing track ID: {}", trackToUpdate.getId());
                } else {
                    // Add new track
                    trackToUpdate = new Track();
                    trackToUpdate.setProject(currentProject);
                    currentProject.getTracks().add(trackToUpdate);
                    log.debug("Adding new track based on snapshot data (original ID if any: {})", newTrack.getId());
                }
                trackToUpdate.setInstrument(newTrack.getInstrument());
                trackToUpdate.setMusicalSequence(newTrack.getMusicalSequence());
            }
            Project revertedProject = projectRepository.save(currentProject);
            log.info("Successfully reverted project ID: {} to version ID: {}", projectId, versionId);
            return revertedProject;
        } catch (JsonProcessingException e) {
            log.error("Error reverting project ID {} to version ID {}: {}", projectId, versionId, e.getMessage());
            throw new RuntimeException("Failed to deserialize snapshot for reverting", e);
        }
    }    
}
