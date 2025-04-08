package com.kanaetochi.audio_alchemists.dto;
import lombok.Data;

@Data
public class RecommendedProjectDto {
    private Long id;
    private String title;
    private String description;
    private String genre;
    private String ownerUsername; // Recommend sending username, not full owner object
    private String reason; // Optional: Why was this recommended? (e.g., "Matches your genre preference", "By user you follow")
}