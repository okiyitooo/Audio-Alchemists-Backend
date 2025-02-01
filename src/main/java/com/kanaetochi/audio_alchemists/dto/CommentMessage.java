package com.kanaetochi.audio_alchemists.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentMessage {
    private Long projectId;
    private String text;
    private Long userId;
}
