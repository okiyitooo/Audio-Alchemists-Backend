package com.kanaetochi.audio_alchemists.dto;

import lombok.Data;

@Data
public class CommentMessage {
    private Long projectId;
    private String text;
    private Long userId;
}
