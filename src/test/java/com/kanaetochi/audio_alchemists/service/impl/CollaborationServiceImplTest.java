package com.kanaetochi.audio_alchemists.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.kanaetochi.audio_alchemists.dto.CollaborationMessage;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Collaboration;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.Role;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.CollaborationRepository;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CollaborationServiceImplTest {
    
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private CollaborationRepository collaborationRepository;

    @InjectMocks
    private CollaborationServiceImpl collaborationService;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<CollaborationMessage> messageCaptor;

    private Project project;
    private User user;
    
    @BeforeEach
    void setup() {
        project = Project.builder()
                .id(1L)
                .title("Test Project")
                .build();

        user = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void testAddCollaborator() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(collaborationRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        Collaboration collaboration = Collaboration.builder()
                .project(project)
                .user(user)
                .role("EDITOR")
                .build();
        when(collaborationRepository.save(any(Collaboration.class))).thenReturn(collaboration);

        Collaboration newCollaboration = collaborationService.addCollaborator(1L, 2L, "EDITOR");

        assertNotNull(newCollaboration);
        assertEquals(newCollaboration.getRole(), "EDITOR");
        verify(projectRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(collaborationRepository, times(1)).existsByProjectIdAndUserId(1L, 2L);
        verify(collaborationRepository, times(1)).save(any(Collaboration.class));
    }

    @Test
    void testAddCollaboratorProjectNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> collaborationService.addCollaborator(1L, 2L, "EDITOR"));
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void testAddCollaboratorUserNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> collaborationService.addCollaborator(1L, 2L, "EDITOR"));
        verify(projectRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void testAddCollaboratorAlreadyExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(collaborationRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);

        assertThrows(ResourceNotFoundException.class, () -> collaborationService.addCollaborator(1L, 2L, "EDITOR"));
        verify(projectRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(collaborationRepository, times(1)).existsByProjectIdAndUserId(1L, 2L);
    }

    @Test
    void testRemoveCollaborator() {
        Collaboration collaboration = Collaboration.builder()
                .project(project)
                .user(user)
                .role("EDITOR")
                .build();

        when(collaborationRepository.findByProjectIdAndUserId(1L, 2L)).thenReturn(Optional.of(collaboration));

        collaborationService.removeCollaborator(1L, 2L);

        verify(collaborationRepository, times(1)).findByProjectIdAndUserId(1L, 2L);
        verify(collaborationRepository, times(1)).delete(collaboration);
    }

    @Test
    void testRemoveCollaboratorNotFound() {
        when(collaborationRepository.findByProjectIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> collaborationService.removeCollaborator(1L, 2L));
        verify(collaborationRepository, times(1)).findByProjectIdAndUserId(1L, 2L);
    }

    @Test
    void testGetCollaboratorsByProject() {
        Collaboration collaboration = Collaboration.builder()
                .project(project)
                .user(user)
                .role("EDITOR")
                .build();
        when(collaborationRepository.findByProjectId(1L)).thenReturn(List.of(collaboration));

        List<Collaboration> collaborations = collaborationService.getCollaboratorsByProject(1L);

        assertFalse(collaborations.isEmpty());
        assertEquals(collaborations.size(), 1);
        verify(collaborationRepository, times(1)).findByProjectId(1L);
    }

//     @Test
//     void testAddCollaboratorSendsCollaborationMessage() {
//         when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
//         when(userRepository.findById(2L)).thenReturn(Optional.of(user));
//         when(collaborationRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

//         Collaboration collaboration = Collaboration.builder()
//                 .project(project)
//                 .user(user)
//                 .role("EDITOR")
//                 .build();
//         when(collaborationRepository.save(any(Collaboration.class))).thenReturn(collaboration);

//         collaborationService.addCollaborator(1L, 2L, "EDITOR");

//         verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

//         assertEquals("/topic/project1", destinationCaptor.getValue()); // Verify the destination

//         CollaborationMessage message = messageCaptor.getValue();
//         assertEquals(1L, message.getProjectId()); // Verify the message content
//         assertEquals(2L, message.getUserId());
//         assertEquals("ADD", message.getActionType());
//         assertEquals("EDITOR", message.getRole());
//     }
//   @Test
//     void testRemoveCollaboratorSendsCollaborationMessage() {
//         Collaboration collaboration = Collaboration.builder()
//                 .project(project)
//                 .user(user)
//                 .role("EDITOR")
//                 .build();

//         when(collaborationRepository.findByProjectIdAndUserId(1L, 2L)).thenReturn(Optional.of(collaboration));

//         collaborationService.removeCollaborator(1L, 2L);
//         verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());
//          assertEquals("/topic/project1", destinationCaptor.getValue()); // Verify the destination
//         CollaborationMessage message = messageCaptor.getValue();
//         assertEquals(1L, message.getProjectId()); // Verify the message content
//         assertEquals(2L, message.getUserId());
//         assertEquals("REMOVE", message.getActionType());
//     }
}
