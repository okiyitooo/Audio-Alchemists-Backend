package com.kanaetochi.audio_alchemists.dto;

import lombok.Data;

@Data
public class JWTAuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";

    public JWTAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}