package com.kanaetochi.audio_alchemists.service;

import java.util.List;
import java.util.Optional;

import com.kanaetochi.audio_alchemists.model.Track;

public interface TrackService {
    Track createTrack(Track track, Long projectId);
    List<Track> getAllTracksByProject(Long projectId);
    Optional<Track> getTrackById(Long id);
    Track updateTrack(Long id, Track trackDetails);
    void deleteTrack(Long id);
}
