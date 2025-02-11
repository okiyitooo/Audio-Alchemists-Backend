package com.kanaetochi.audio_alchemists.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
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
public class ProjectControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    private String token;
    private String id;

    @BeforeEach
    void setup() throws Exception {
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
        String response =  mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
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
        mockMvc.perform(MockMvcRequestBuilders.post("/projects")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated());
        id = projectRepository.findAll().get(0).getId().toString();
        
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testCreateProject() throws Exception {
        Project project = Project.builder()
                .title("testproject")
                .description("testproject description")
                .genre("testgenre")
                .tempo(120)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.post("/projects")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("testproject"))
                .andExpect(jsonPath("$.description").value("testproject description"));

    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"COMPOSER"})
    void testGetAllProjects() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProjectById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/{id}", id)
                .param("id", id)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("testproject"));
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetProjectByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/projects/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateProject() throws Exception {
        Project updatedProject = Project.builder()
                .title("updatedProject")
                .description("updatedDescription")
                .genre("updatedGenre")
                .tempo(130)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.put("/projects/{id}", id)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("updatedProject"))
                .andExpect(jsonPath("$.description").value("updatedDescription"));

    }
        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void testUpdateProjectNotFound() throws Exception {
                Project updatedProject = Project.builder()
                        .title("updatedProject")
                        .description("updatedDescription")
                        .genre("updatedGenre")
                        .tempo(130)
                        .build();
                mockMvc.perform(MockMvcRequestBuilders.put("/projects/2")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedProject)))
                        .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void testDeleteProject() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.delete("/projects/{id}", id)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Project deleted successfully"));
                assertEquals(projectRepository.findAll().size(), 0);
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void testDeleteProjectNotFound() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.delete("/projects/2")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void testAddCollaborator() throws Exception {
                RegisterDto registerDto = new RegisterDto();
                registerDto.setUsername("collaborator");
                registerDto.setEmail("collaborator@example.com");
                registerDto.setPassword("password");
                registerDto.setRole("COMPOSER");
                mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDto)))
                        .andExpect(status().isOk());
                LoginDto loginDto = new LoginDto();
                loginDto.setUsernameOrEmail("collaborator");
                loginDto.setPassword("password");
                Long userId = userRepository.findByUsername("collaborator").get().getId();
                mockMvc.perform(MockMvcRequestBuilders.post("/projects/{id}/collaborators?userId={userId}&role=EDITOR", id, userId)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Collaborator added successfully"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        void testRemoveCollaborator() throws Exception {
                RegisterDto registerDto = new RegisterDto();
                registerDto.setUsername("collaborator");
                registerDto.setEmail("collaborator@example.com");
                registerDto.setPassword("password");
                registerDto.setRole("COMPOSER");
                mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDto)))
                        .andExpect(status().isOk());
                LoginDto loginDto = new LoginDto();
                loginDto.setUsernameOrEmail("collaborator");
                loginDto.setPassword("password");
                Long userId = userRepository.findByUsername("collaborator").get().getId();
                mockMvc.perform(MockMvcRequestBuilders.post("/projects/{id}/collaborators?userId={userId}&role=EDITOR", id, userId)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Collaborator added successfully"));
                mockMvc.perform(MockMvcRequestBuilders.delete("/projects/{id}/collaborators?userId={userId}&role=EDITOR", id, userId)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().string("Collaborator removed successfully"));

        }
}