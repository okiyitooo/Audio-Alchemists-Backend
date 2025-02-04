package com.kanaetochi.audio_alchemists.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.kanaetochi.audio_alchemists.security.JwtHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // endpoint for clients to connect to the WebSocket server
                .setAllowedOriginPatterns("*")  // allow all origins (Later I'll restrict this to only our frontend domain)
                .addInterceptors(jwtHandshakeInterceptor) // add interceptor to validate JWT token
                .withSockJS()
                ; // enable SockJS fallback options for browsers that don't support WebSocket
    }
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // prefix for messages going to the application (server)
        registry.enableSimpleBroker("/topic", "/queue"); // prefix for messages going to the client
    }
}
