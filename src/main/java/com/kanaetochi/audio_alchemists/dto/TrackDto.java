package com.kanaetochi.audio_alchemists.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TrackDto {
    private Long id;
    private String instrument;
    private String musicalSequence;
    private ProjectDto project;
    private LocalDateTime updatedAt;
}