package com.kanaetochi.audio_alchemists.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.TrackRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TrackControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private String token;
    private Long projectId;
    private Long trackId;

    @BeforeEach
    void setup() throws Exception {
        trackRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("testuser@example.com");
        registerDto.setPassword("password");
        registerDto.setRole("COMPOSER");
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password");
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
         token = "Bearer " + objectMapper.readTree(response).get("accessToken").asText();

        Project project = Project.builder()
               .title("testproject")
               .description("testproject description")
               .genre("testgenre")
               .tempo(120)
               .build();
        String projectResponse = mockMvc.perform(MockMvcRequestBuilders.post("/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
       projectId = objectMapper.readTree(projectResponse).get("id").asLong();
       Track track = Track.builder()
               .instrument("piano")
               .musicalSequence("{\"notes\": [{\"pitch\":60,\"velocity\":100}]}")
               .project(project)
               .build();
         mockMvc.perform(MockMvcRequestBuilders.post("/projects/" + projectId + "/tracks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(track)))
                .andExpect(status().isCreated());
        trackId = trackRepository.findAll().get(0).getId();
    }


   @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testCreateTrack() throws Exception {

        Track track = Track.builder()
                .instrument("piano")
                .musicalSequence("{\"notes\": [{\"pitch\":60,\"velocity\":100}]}")
               .build();
        mockMvc.perform(MockMvcRequestBuilders.post("/projects/" + projectId + "/tracks")
               .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(track)))
                .andExpect(status().isCreated())
               .andExpect(jsonPath("$.instrument").value("piano"))
                .andExpect(jsonPath("$.musicalSequence").value("{\"notes\": [{\"pitch\":60,\"velocity\":100}]}"));


    }

   @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testGetAllTracks() throws Exception {
       mockMvc.perform(MockMvcRequestBuilders.get("/projects/" + projectId + "/tracks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

    }
    @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testGetTrackById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/" + projectId + "/tracks/{trackId}", trackId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
               .andExpect(jsonPath("$.instrument").value("piano"));
    }
    @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testGetTrackByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/" + projectId + "/tracks/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testUpdateTrack() throws Exception {
        Track updatedTrack = Track.builder()
                        .instrument("guitar")
                       .musicalSequence("{\"notes\": [{\"pitch\":61,\"velocity\":90}]}")
                                .build();
        mockMvc.perform(MockMvcRequestBuilders.put("/projects/" + projectId + "/tracks/{id}", trackId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTrack)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrument").value("guitar"))
                .andExpect(jsonPath("$.musicalSequence").value("{\"notes\": [{\"pitch\":61,\"velocity\":90}]}"));

    }
    @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testUpdateTrackNotFound() throws Exception {
        Track updatedTrack = Track.builder()
                .instrument("guitar")
                .musicalSequence("{\"notes\": [{\"pitch\":61,\"velocity\":90}]}")
                .build();
        mockMvc.perform(MockMvcRequestBuilders.put("/projects/" + projectId + "/tracks/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTrack)))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testDeleteTrack() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/projects/" + projectId + "/tracks/{id}", trackId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Track deleted successfully"));
      assertEquals(trackRepository.findAll().size(),0);
    }

   @Test
   @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
   void testDeleteTrackNotFound() throws Exception {
       mockMvc.perform(MockMvcRequestBuilders.delete("/projects/" + projectId + "/tracks/2")
                       .header("Authorization", token)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
   }

}