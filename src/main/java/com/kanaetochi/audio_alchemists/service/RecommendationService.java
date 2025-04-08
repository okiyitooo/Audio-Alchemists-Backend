package com.kanaetochi.audio_alchemists.service;

import com.kanaetochi.audio_alchemists.dto.RecommendedProjectDto;
import com.kanaetochi.audio_alchemists.dto.RecommendedUserDto;
import com.kanaetochi.audio_alchemists.model.User;

import java.util.List;

public interface RecommendationService {
    List<RecommendedProjectDto> getProjectRecommendations(User user, int limit);
    List<RecommendedUserDto> getUserRecommendations(User user, int limit);
}