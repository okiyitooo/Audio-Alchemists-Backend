package com.kanaetochi.audio_alchemists.exception;

public class ConcurrentEditException extends RuntimeException {
    public ConcurrentEditException(String message) {
        super(message);
    }
}
