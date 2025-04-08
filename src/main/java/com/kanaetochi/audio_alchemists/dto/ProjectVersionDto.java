package com.kanaetochi.audio_alchemists.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProjectVersionDto {
    private Long id;
    private String description;
    private LocalDateTime timeStamp;
    private String savedByUsername;
}
