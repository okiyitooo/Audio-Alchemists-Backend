package com.kanaetochi.audio_alchemists.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrackChangeMessage {
    private Long trackId;
    private String changeType; // "ADD", "DELETE", "MODIFY"
    private String data;
    private Long userId;
}
