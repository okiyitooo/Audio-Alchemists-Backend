package com.kanaetochi.audio_alchemists.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}