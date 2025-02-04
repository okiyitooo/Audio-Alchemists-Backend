package com.kanaetochi.audio_alchemists.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private Long projectId;
    private Long userId;
    private LocalDateTime timestamp;
}
