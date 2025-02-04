package com.kanaetochi.audio_alchemists;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class AudioAlchemistsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AudioAlchemistsApplication.class, args);
	}

	@Bean
	public MessageChannel messageChannel() {
		ExecutorSubscribableChannel channel = new ExecutorSubscribableChannel(taskExecutor());
		channel.setInterceptors(List.of(new ChannelInterceptor() {
			@Override
			public void afterSendCompletion(@NonNull org.springframework.messaging.Message<?> message, @NonNull MessageChannel channel, boolean sent, @Nullable Exception ex) {
				if (ex != null) {
					System.out.println("Error sending message: " + message + " " + ex);
				}
			}
		}));
		return channel;
	}

	@Bean 
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(200);
		executor.setThreadNamePrefix("websocket-thread-");
		executor.initialize();
		return executor;
	}

	@Bean
	@Primary
	public SimpMessagingTemplate messagingTemplate(MessageChannel messageChannel) {
		return new SimpMessagingTemplate(messageChannel);
	}

}
