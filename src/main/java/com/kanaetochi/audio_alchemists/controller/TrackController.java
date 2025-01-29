package com.kanaetochi.audio_alchemists.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.service.TrackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {
    
    private final TrackService trackService;
    
    @PostMapping
    public ResponseEntity<Track> createTrack(@PathVariable Long projectId, @RequestBody Track track){
       Track newTrack = trackService.createTrack(track, projectId);
        return new ResponseEntity<>(newTrack, HttpStatus.CREATED);
    }



    @GetMapping
    public ResponseEntity<?> getAllTracks(@PathVariable Long projectId){
        return new ResponseEntity<>(trackService.getAllTracksByProject(projectId), HttpStatus.OK);
    }

   @GetMapping("/{id}")
    public ResponseEntity<Track> getTrackById(@PathVariable Long id){
        return trackService.getTrackById(id)
                .map(track -> new ResponseEntity<>(track, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


   @PutMapping("/{id}")
    public ResponseEntity<Track> updateTrack(@PathVariable Long id, @RequestBody Track trackDetails){
        return ResponseEntity.ok().body(trackService.updateTrack(id, trackDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrack(@PathVariable Long id){
        trackService.deleteTrack(id);
        return  new ResponseEntity<>("Track deleted successfully", HttpStatus.OK);
    }
}
