package com.kanaetochi.audio_alchemists.controller;

import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.service.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/projects/{projectId}/tracks")
public class TrackController {


    final private TrackService trackService;
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('COMPOSER')") // Only composers can create tracks
    public ResponseEntity<Track> createTrack(@PathVariable Long projectId, @RequestBody Track track){
       Track newTrack = trackService.createTrack(track, projectId);
        return new ResponseEntity<>(newTrack, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<Track>> getAllTracks(@PathVariable Long projectId){
      List<Track> tracks =  trackService.getAllTracksByProject(projectId);
        return new ResponseEntity<>(tracks, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id){
      Optional<Track> track = trackService.getTrackById(id);
       return track.map(t -> new ResponseEntity<>(t, HttpStatus.OK))
               .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPOSER')") // Only composers can update their tracks
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @RequestBody Track trackDetails){
      Optional<Track> existingTrack = trackService.getTrackById(id);
      return existingTrack.map(t -> {
          Track updatedTrack = trackService.updateTrack(id,trackDetails);
          return  new ResponseEntity<>(updatedTrack, HttpStatus.OK);
      }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPOSER')")  // Only composers can delete tracks
    public ResponseEntity<String> deleteTrack(@PathVariable Long id){
        trackService.deleteTrack(id);
        return  new ResponseEntity<>("Track deleted successfully", HttpStatus.OK);
    }

}