package com.kanaetochi.audio_alchemists.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.dto.LoginDto;
import com.kanaetochi.audio_alchemists.dto.RegisterDto;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.User;
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
public class UserControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    private String token;


    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("testuser@example.com");
        registerDto.setPassword("password");
        registerDto.setRole("ADMIN");
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
    }



   @Test
   @WithMockUser(username = "testuser", roles = {"ADMIN"})
   void testGetAllUsers() throws Exception {
     mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetAllUsersUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }
   @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testGetUserById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }
    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testUpdateUser() throws Exception {
        User updatedUser = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .role(Role.COMPOSER)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.role").value("COMPOSER"));
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testUpdateUserNotFound() throws Exception {
        User updatedUser = User.builder()
                .username("updateduser")
                .email("updated@example.com")
                .role(Role.COMPOSER)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.put("/users/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testDeleteUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User successfully deleted!"));
     assertEquals(userRepository.findAll().size(),0);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testDeleteUserNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/2")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}