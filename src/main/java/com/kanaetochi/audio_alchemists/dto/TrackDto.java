package com.kanaetochi.audio_alchemists.dto;

import lombok.Data;

@Data
public class TrackDto {
    private Long id;
    private String instrument;
    private String musicalSequence;
    private ProjectDto project;
}