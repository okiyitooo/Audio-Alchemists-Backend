package com.kanaetochi.audio_alchemists.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.TrackRepository;

@ExtendWith(MockitoExtension.class)
public class TrackServiceImplTest {
    
    @Mock
    private TrackRepository trackRepository;
    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TrackServiceImpl trackService;

    private Track track;
    private Project project;

    @BeforeEach
    void setUp() {
        User user = User.builder()
            .id(1l)
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .role(Role.USER)
            .build();
        project = Project.builder()
            .id(1l)
            .title("Test Project")
            .description("test description")
            .genre("test genre")
            .tempo(120)
            .owner(user)
            .build();
        track = Track.builder()
            .id(1l)
            .instrument("Piano")
            .musicalSequence("{}")
            .project(project)
            .build();
    }

    @Test
    void testCreateTrack() {
        when(projectRepository.findById(1l)).thenReturn(Optional.of(project));
        when(trackRepository.save(any(Track.class))).thenReturn(track);
        Track newTrack = trackService.createTrack(track, 1l);
        assertNotNull(newTrack);
        assertEquals(newTrack.getInstrument(), track.getInstrument());
        assertEquals(newTrack.getMusicalSequence(), track.getMusicalSequence());
        assertEquals(newTrack.getProject(), track.getProject());
        verify(trackRepository, times(1)).save(any(Track.class));
        verify(projectRepository, times(1)).findById(1l);
    }
    @Test
    void testCreateTrackNotFound() {
        when(projectRepository.findById(1l)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,() -> trackService.createTrack(track, 1l));
        verify(trackRepository, times(0)).save(any(Track.class));
        verify(projectRepository, times(1)).findById(1l);
    }
    @Test
    void testGetAllTracksByProject() {
        when(trackRepository.findAll()).thenReturn(List.of(track));
        List<Track> tracks = trackService.getAllTracksByProject(1l);
        assertFalse(tracks.isEmpty());
        assertEquals(tracks.size(), 1);
        verify(trackRepository, times(1)).findAll();
    }
    @Test
    void testGetTrackById() {
        when(trackRepository.findById(1l)).thenReturn(Optional.of(track));
        Optional<Track> retrievedTrack = trackService.getTrackById(1l);
        assertTrue(retrievedTrack.isPresent());
        assertEquals(retrievedTrack.get().getInstrument(), track.getInstrument());
        assertEquals(retrievedTrack.get().getMusicalSequence(), track.getMusicalSequence());
        verify(trackRepository, times(1)).findById(1l);
    }
    @Test
    void testGetTrackByIdNotFound() {
        when(trackRepository.findById(1l)).thenReturn(Optional.empty());
        Optional<Track> retrievedTrack = trackService.getTrackById(1L);
        assertTrue(retrievedTrack.isEmpty());
        verify(trackRepository, times(1)).findById(1l);
    }
    @Test
    void testUpdateTrack() {
        Track trackDetails = Track.builder()
            .instrument("Guitar")
            .musicalSequence("{\"notes\": [{\"pitch\":60,\"velocity\":100}]}")
            .build();
        when(trackRepository.findById(1l)).thenReturn(Optional.of(track));
        when(trackRepository.save(any(Track.class))).thenReturn(track);
        Track updatedTrack = trackService.updateTrack(1l, trackDetails);
        assertNotNull(updatedTrack);
        assertEquals(updatedTrack.getInstrument(), trackDetails.getInstrument());
        assertEquals(updatedTrack.getMusicalSequence(), trackDetails.getMusicalSequence());
        verify(trackRepository, times(1)).save(any(Track.class));
        verify(trackRepository, times(1)).findById(1l);
    }
    @Test
    void testUpdateTrackNotFound() {
        Track trackDetails = Track.builder()
            .instrument("Guitar")
            .musicalSequence("{\"notes\": [{\"pitch\":60,\"velocity\":100}]}")
            .build();
        when(trackRepository.findById(1l)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trackService.updateTrack(1l, trackDetails));
        verify(trackRepository, times(0)).save(any(Track.class));
        verify(trackRepository, times(1)).findById(1l);
    }
    @Test
    void testDeleteTrack() {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        trackService.deleteTrack(1L);
        verify(trackRepository, times(1)).findById(1L);
        verify(trackRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTrackNotFound(){
        when(trackRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trackService.deleteTrack(1L));
        verify(trackRepository, times(1)).findById(1L);
    }
}
