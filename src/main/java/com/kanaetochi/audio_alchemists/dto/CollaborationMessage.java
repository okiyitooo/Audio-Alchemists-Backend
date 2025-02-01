package com.kanaetochi.audio_alchemists.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollaborationMessage {
    private Long projectId;
    private Long userId;
    private String actionType; // "ADD", "REMOVE"
    private String role;
}
