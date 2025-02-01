package com.kanaetochi.audio_alchemists.controller;

import com.kanaetochi.audio_alchemists.dto.TrackChangeMessage;
import com.kanaetochi.audio_alchemists.dto.TrackDto;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.service.TrackService;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/projects/{projectId}/tracks")
@RequiredArgsConstructor
public class TrackController {


    final private TrackService trackService;
    final private ModelMapper modelMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPOSER')") // Only composers can create tracks
    public ResponseEntity<TrackDto> createTrack(@PathVariable Long projectId, @RequestBody Track track){
        Track newTrack = trackService.createTrack(track, projectId);
        TrackDto trackDto = modelMapper.map(newTrack, TrackDto.class);
        sendTrackChangeMessage(newTrack.getId(), "ADD", trackDto);
        return new ResponseEntity<>(trackDto, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<TrackDto>> getAllTracks(@PathVariable Long projectId){
        List<Track> tracks =  trackService.getAllTracksByProject(projectId);
        List<TrackDto> trackDtos = tracks.stream().map(track -> modelMapper.map(track, TrackDto.class)).toList();
        return new ResponseEntity<>(trackDtos, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TrackDto> getTrackById(@PathVariable Long id){
        Optional<Track> track = trackService.getTrackById(id);
        return track.map(t -> new ResponseEntity<>(modelMapper.map(t, TrackDto.class), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPOSER')") // Only composers can update their tracks
    public ResponseEntity<TrackDto> updateTrack(@PathVariable Long id, @RequestBody Track trackDetails){
        Track updatedTrack = trackService.updateTrack(id, trackDetails);
        TrackDto trackDto = modelMapper.map(updatedTrack, TrackDto.class);
        sendTrackChangeMessage(updatedTrack.getId(), "MODIFY", trackDto);
        return new ResponseEntity<>(trackDto, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPOSER')")  // Only composers can delete tracks
    public ResponseEntity<String> deleteTrack(@PathVariable Long id){
        trackService.deleteTrack(id);
        sendTrackChangeMessage(id, "DELETE", id);
        return  new ResponseEntity<>("Track deleted successfully", HttpStatus.OK);
    }

    private void sendTrackChangeMessage(Long trackId, String changeType, Object data) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            userId = Long.parseLong(user.getUsername());
        }
        TrackChangeMessage trackChangeMessage = TrackChangeMessage.builder()
                .trackId(trackId)
                .changeType(changeType)
                .data(data.toString())
                .userId(userId)
                .build();
        simpMessagingTemplate.convertAndSend("/topic/track" + trackId, trackChangeMessage);
    }
}