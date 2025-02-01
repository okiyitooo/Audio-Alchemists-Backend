package com.kanaetochi.audio_alchemists;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootApplication
public class AudioAlchemistsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AudioAlchemistsApplication.class, args);
	}

	@Bean
	public SimpMessagingTemplate messagingTemplate(MessageChannel messageChannel) {
		return new SimpMessagingTemplate(messageChannel);
	}

}
