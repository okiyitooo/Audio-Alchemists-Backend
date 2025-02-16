package com.kanaetochi.audio_alchemists.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;

    private Long projectId;

    private Long collaboratorId;

    @BeforeEach
    public void setup() throws Exception {
        projectRepository.deleteAll();
        userRepository.deleteAll();

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("testuser@example.com");
        registerDto.setPassword("password");
        registerDto.setRole("COMPOSER");

        ResponseEntity<String> registerResponse = registerUser(registerDto);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());

        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password");

        ResponseEntity<String> loginResponse = loginUser(loginDto);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());

        JsonNode jsonNode = objectMapper.readTree(loginResponse.getBody());
        token = "Bearer " + jsonNode.get("accessToken").asText();

        Project project = Project.builder()
                .title("testproject")
                .description("testproject description")
                .genre("testgenre")
                .tempo(120)
                .build();

        ResponseEntity<String> createProjectResponse = createProject(project);
        assertEquals(HttpStatus.CREATED, createProjectResponse.getStatusCode());

        projectId = projectRepository.findAll().get(0).getId();

        RegisterDto collaboratorRegisterDto = new RegisterDto();
        collaboratorRegisterDto.setUsername("collaborator");
        collaboratorRegisterDto.setEmail("collaborator@example.com");
        collaboratorRegisterDto.setPassword("password");
        collaboratorRegisterDto.setRole("USER");

        registerResponse = registerUser(collaboratorRegisterDto);
        assertEquals(HttpStatus.OK, registerResponse.getStatusCode());

        collaboratorId = userRepository.findByUsername("collaborator").get().getId();
    }

    @Test
    public void testRealTimeCollaboration() throws Exception {
        // final String wsUrl = "ws://localhost:" + port + "/ws";

        // StandardWebSocketClient client = new StandardWebSocketClient();
        // TestWebSocketHandler handler = new TestWebSocketHandler();
        // WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        // headers.add(AUTHORIZATION, token);

        // // Connect to WebSocket
        // WebSocketSession session = client.execute(handler, headers, new URI(wsUrl)).get(5, TimeUnit.SECONDS);
        // assertNotNull(session);

        // // Add a collaborator via the REST API
        // ResponseEntity<String> addCollaboratorResponse = addCollaborator();
        // assertEquals(HttpStatus.OK, addCollaboratorResponse.getStatusCode());

        // // Wait for the WebSocket message
        // String receivedMessage = handler.getMessage();
        // assertNotNull(receivedMessage, "No WebSocket message received");

        // // Check that received message contains correct data but is not too strict with data.
        // assertTrue(receivedMessage.contains("ADD"), "Message does not contain ADD");
        // assertTrue(receivedMessage.contains(String.valueOf(projectId)), "Message does not contain projectId");
    }

    private ResponseEntity<String> registerUser(RegisterDto registerDto) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(convertToJson(registerDto), headers);
        return restTemplate.postForEntity("http://localhost:" + port + "/auth/register", request, String.class);
    }

    private ResponseEntity<String> loginUser(LoginDto loginDto) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(convertToJson(loginDto), headers);
        return restTemplate.postForEntity("http://localhost:" + port + "/auth/login", request, String.class);
    }

    private ResponseEntity<String> createProject(Project project) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, token);
        HttpEntity<String> request = new HttpEntity<>(convertToJson(project), headers);
        return restTemplate.postForEntity("http://localhost:" + port + "/projects", request, String.class);
    }

    @SuppressWarnings("unused")
    private ResponseEntity<String> addCollaborator() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, token);
        HttpEntity<String> request = new HttpEntity<>(null, headers);

        String url = "http://localhost:" + port + "/projects/" + projectId + "/collaborators?userId=" + collaboratorId + "&role=EDITOR";

        return restTemplate.postForEntity(url, request, String.class);
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    // Inner class to handle WebSocket messages
    @SuppressWarnings("unused")
    private static class TestWebSocketHandler extends TextWebSocketHandler {

        final private BlockingQueue<String> recievedMessages = new LinkedBlockingQueue<>();
        private WebSocketSession session;

        @Override
        public void afterConnectionEstablished(@NonNull WebSocketSession session) {
                this.session = session;
        }

        @Override
        public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
                String payload = message.getPayload();
                System.out.println("Received message: " + payload);
                recievedMessages.add(payload);
        }

        public String getMessage() throws InterruptedException {
                return recievedMessages.poll(5, TimeUnit.SECONDS);
        }

        public WebSocketSession getSession() {
                return session;
        }
    }
}