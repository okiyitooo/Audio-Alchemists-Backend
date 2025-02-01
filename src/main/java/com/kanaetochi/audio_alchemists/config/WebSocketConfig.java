package com.kanaetochi.audio_alchemists.config;

import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint for clients to connect to the WebSocket server
                .setAllowedOrigins("*")  // allow all origins (Later I'll restrict this to only our frontend domain)
                .withSockJS(); // enable SockJS fallback options for browsers that don't support WebSocket
    }
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app") // prefix for messages going to the application (server)
                .enableSimpleBroker("/topic"); // prefix for messages going to the client
    }
}
