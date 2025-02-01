package com.kanaetochi.audio_alchemists.dto;

import lombok.Data;

@Data
public class ProjectDto {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private Integer tempo;
    private UserDto owner;
}