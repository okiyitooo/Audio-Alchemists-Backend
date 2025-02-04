package com.kanaetochi.audio_alchemists.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.exception.ConcurrentEditException;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.TrackRepository;
import com.kanaetochi.audio_alchemists.service.TrackService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final ProjectRepository projectRepository;

    @Override
    public Track createTrack(Track track, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        track.setProject(project);
        return trackRepository.save(track);
    }

    @Override
    public List<Track> getAllTracksByProject(Long projectId) {
        return trackRepository.findAll().stream().filter(track -> track.getProject().getId().equals(projectId)).toList();
    }

    @Override
    public Optional<Track> getTrackById(Long id) {
        return trackRepository.findById(id);
    }

    @Override
    public Track updateTrack(Long id, Track trackDetails) {
        try {
            Track track = trackRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Track", "id", id));
            track.setInstrument(trackDetails.getInstrument());
            track.setMusicalSequence(trackDetails.getMusicalSequence());
            return trackRepository.save(track);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcurrentEditException("The track has been modified by another user. Please refresh your changes.");
        }
    }

    @Override
    public void deleteTrack(Long id) {
        trackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Track", "id", id));
        trackRepository.deleteById(id);
    }

}
