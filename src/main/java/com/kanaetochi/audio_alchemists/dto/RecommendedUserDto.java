package com.kanaetochi.audio_alchemists.dto;
import lombok.Data;

@Data
public class RecommendedUserDto {
    private Long id;
    private String username;
    private String reason; // "Collaborates on similar projects"? "Followed by users you follow"?
}