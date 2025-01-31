package com.kanaetochi.audio_alchemists.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;
    private User user;

    @BeforeEach
    void setup(){
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
       project = Project.builder()
               .id(1L)
               .title("testproject")
               .description("testproject description")
               .genre("testgenre")
               .tempo(120)
               .owner(user)
               .build();

    }

    @Test
    void testCreateProject(){
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        Project newProject = projectService.createProject(project, 1L);
        assertNotNull(newProject);
        assertEquals(newProject.getTitle(), project.getTitle());
        assertEquals(newProject.getOwner(), project.getOwner());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(userRepository, times(1)).findById(1L);

    }

    @Test
    void testCreateProjectUserNotFound(){
       when(userRepository.findById(1L)).thenReturn(Optional.empty());
       assertThrows(ResourceNotFoundException.class, () -> projectService.createProject(project, 1L));
       verify(userRepository, times(1)).findById(1L);
       verify(projectRepository, times(0)).save(any(Project.class));
    }



    @Test
    void testGetAllProjects(){
        when(projectRepository.findAll()).thenReturn(List.of(project));
        List<Project> projects = projectService.getAllProjects();
        assertFalse(projects.isEmpty());
        assertEquals(projects.size(),1);
        verify(projectRepository, times(1)).findAll();

    }

    @Test
    void testGetProjectById(){
       when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
       Optional<Project> retrievedProject = projectService.getProjectById(1L);
       assertTrue(retrievedProject.isPresent());
       assertEquals(retrievedProject.get().getTitle(), project.getTitle());
        verify(projectRepository, times(1)).findById(1L);
    }
   @Test
    void testGetProjectByIdNotFound(){
       when(projectRepository.findById(1L)).thenReturn(Optional.empty());
       Optional<Project> retrievedProject = projectService.getProjectById(1L);
       assertTrue(retrievedProject.isEmpty());
        verify(projectRepository, times(1)).findById(1L);

    }

    @Test
    void testUpdateProject(){
        Project updatedProjectDetails = Project.builder()
                .title("updatedTitle")
                .description("updatedDescription")
                .genre("updatedGenre")
                .tempo(120)
                .build();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
       Project updatedProject = projectService.updateProject(1L,updatedProjectDetails);
        assertNotNull(updatedProject);
        assertEquals(updatedProject.getTitle(), updatedProjectDetails.getTitle());
        assertEquals(updatedProject.getDescription(), updatedProjectDetails.getDescription());
        assertEquals(updatedProject.getGenre(), updatedProjectDetails.getGenre());
        verify(projectRepository, times(1)).findById(1L);
       verify(projectRepository, times(1)).save(any(Project.class));

    }

    @Test
    void testUpdateProjectNotFound(){
        Project updatedProjectDetails = Project.builder()
                .title("updatedTitle")
                .description("updatedDescription")
                .genre("updatedGenre")
                .tempo(120)
                .build();
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(1L,updatedProjectDetails));
         verify(projectRepository, times(1)).findById(1L);
         verify(projectRepository, times(0)).save(any(Project.class));
    }


    @Test
    void testDeleteProject(){
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
       projectService.deleteProject(1L);
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProjectNotFound(){
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(1L));
        verify(projectRepository, times(1)).findById(1L);
    }
}