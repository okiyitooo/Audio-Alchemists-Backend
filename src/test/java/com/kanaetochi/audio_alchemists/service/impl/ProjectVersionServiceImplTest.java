package com.kanaetochi.audio_alchemists.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Project;
import com.kanaetochi.audio_alchemists.model.ProjectVersion;
import com.kanaetochi.audio_alchemists.model.Track;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.ProjectVersionRepository;
import com.kanaetochi.audio_alchemists.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // For setting static ThreadLocal if needed

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectVersionServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectVersionRepository projectVersionRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private ObjectMapper objectMapper; // Mock Jackson ObjectMapper

    @InjectMocks // Inject mocks into the service
    private ProjectVersionServiceImpl projectVersionService;

    @Captor // Capture arguments passed to mocks
    private ArgumentCaptor<ProjectVersion> projectVersionCaptor;
    @Captor
    private ArgumentCaptor<Project> projectCaptor;
    @Captor
    private ArgumentCaptor<List<Track>> trackListCaptor;


    private User user;
    private Project project;
    private Track track1, track2;
    private String sampleProjectJson;
    private Project snapshotProject; // Represents the deserialized project state

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        track1 = Track.builder().id(101L).instrument("Piano").musicalSequence("seq1").build();
        track2 = Track.builder().id(102L).instrument("Drums").musicalSequence("seq2").build();
        project = Project.builder()
                .id(1L)
                .title("Original Project")
                .description("Original Desc")
                .tempo(120)
                .tracks(new ArrayList<>(List.of(track1, track2))) // Use mutable list
                .owner(user)
                .build();
        // Set bidirectional relationship for testing serialization/reconciliation
        track1.setProject(project);
        track2.setProject(project);

        sampleProjectJson = "{\"id\":1,\"title\":\"Snapshot Project\",\"description\":\"Snapshot Desc\",\"tempo\":100,\"tracks\":[{\"id\":101,\"instrument\":\"Piano Updated\",\"musicalSequence\":\"seq1_updated\"},{\"id\":103,\"instrument\":\"Bass\",\"musicalSequence\":\"seq3_new\"}]}";

        // Setup the snapshotProject based on sampleProjectJson for revert tests
        Track snapshotTrack1 = Track.builder().id(101L).instrument("Piano Updated").musicalSequence("seq1_updated").build();
        Track snapshotTrack3 = Track.builder().id(103L).instrument("Bass").musicalSequence("seq3_new").build(); // New track in snapshot
        snapshotProject = Project.builder()
                .id(1L) // ID might not matter directly from JSON but good practice
                .title("Snapshot Project")
                .description("Snapshot Desc")
                .tempo(100)
                .tracks(new ArrayList<>(List.of(snapshotTrack1, snapshotTrack3)))
                .build();
        snapshotTrack1.setProject(snapshotProject); // Set back-reference if needed by logic
        snapshotTrack3.setProject(snapshotProject);
    }

    // --- createSnapshot Tests ---

    @Test
    @DisplayName("createSnapshot should serialize project and save version successfully")
    void createSnapshot_Success() throws JsonProcessingException {
        // Arrange
        String expectedJson = "{\"id\":1,\"title\":\"Original Project\"}"; // Simplified JSON for test
        String description = "Test Snapshot";
        ProjectVersion savedVersion = ProjectVersion.builder().id(1L).snapshotData(expectedJson).build();

        when(objectMapper.writeValueAsString(project)).thenReturn(expectedJson);
        when(projectVersionRepository.save(any(ProjectVersion.class))).thenReturn(savedVersion);

        // Act
        ProjectVersion result = projectVersionService.createSnapShot(project, user, description);

        // Assert
        assertNotNull(result);
        assertEquals(expectedJson, result.getSnapshotData());
        verify(objectMapper, times(1)).writeValueAsString(project);
        verify(projectVersionRepository, times(1)).save(projectVersionCaptor.capture());

        ProjectVersion capturedVersion = projectVersionCaptor.getValue();
        assertEquals(project, capturedVersion.getProject());
        assertEquals(user, capturedVersion.getSavedBy());
        assertEquals(description, capturedVersion.getDescription());
        assertEquals(expectedJson, capturedVersion.getSnapshotData());
        // timestamp is set by @PrePersist, implicitly tested by save call
    }

    @Test
    @DisplayName("createSnapshot should throw RuntimeException on serialization error")
    void createSnapshot_SerializationError() throws JsonProcessingException {
        // Arrange
        String description = "Test Snapshot";
        when(objectMapper.writeValueAsString(project)).thenThrow(new JsonProcessingException("Serialization failed") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectVersionService.createSnapShot(project, user, description);
        });
        assertEquals("Failed to serialize project for snapshot", exception.getMessage());
        verify(objectMapper, times(1)).writeValueAsString(project);
        verify(projectVersionRepository, never()).save(any()); // Ensure save was not called
    }

    // --- getVersionsForProject Tests ---

    @Test
    @DisplayName("getVersionsForProject should return list of versions ordered by timestamp desc")
    void getVersionsForProject_Success() {
        // Arrange
        Long projectId = 1L;
        ProjectVersion v1 = ProjectVersion.builder().id(1L).timeStamp(LocalDateTime.now().minusDays(1)).build();
        ProjectVersion v2 = ProjectVersion.builder().id(2L).timeStamp(LocalDateTime.now()).build();
        List<ProjectVersion> mockedVersions = List.of(v2, v1); // Desc order

        when(projectVersionRepository.findByProjectIdOrderByTimeStampDesc(projectId)).thenReturn(mockedVersions);

        // Act
        List<ProjectVersion> results = projectVersionService.getVersionsForProject(projectId);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(v2, results.get(0)); // Verify order
        assertEquals(v1, results.get(1));
        verify(projectVersionRepository, times(1)).findByProjectIdOrderByTimeStampDesc(projectId);
    }

    @Test
    @DisplayName("getVersionsForProject should return empty list when no versions exist")
    void getVersionsForProject_NoVersions() {
        // Arrange
        Long projectId = 1L;
        when(projectVersionRepository.findByProjectIdOrderByTimeStampDesc(projectId)).thenReturn(Collections.emptyList());

        // Act
        List<ProjectVersion> results = projectVersionService.getVersionsForProject(projectId);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(projectVersionRepository, times(1)).findByProjectIdOrderByTimeStampDesc(projectId);
    }

    // --- revertToVersion Tests ---

    @Test
    @DisplayName("revertToVersion should successfully update project and reconcile tracks")
    void revertToVersion_Success() throws JsonProcessingException {
        // Arrange
        Long projectId = 1L;
        Long versionId = 5L;
        ProjectVersion versionToRevertTo = ProjectVersion.builder()
                .id(versionId)
                .project(project) // Link version to the project
                .snapshotData(sampleProjectJson)
                .timeStamp(LocalDateTime.now())
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectVersionRepository.findById(versionId)).thenReturn(Optional.of(versionToRevertTo));
        when(objectMapper.readValue(sampleProjectJson, Project.class)).thenReturn(snapshotProject);
        // Mock save to return the captured project
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Project revertedProject = projectVersionService.revertToVersion(projectId, versionId, user);

        // Assert
        assertNotNull(revertedProject);

        // Verify metadata update
        assertEquals("Snapshot Project", revertedProject.getTitle());
        assertEquals("Snapshot Desc", revertedProject.getDescription());
        assertEquals(100, revertedProject.getTempo());

        // Verify track reconciliation
        assertEquals(2, revertedProject.getTracks().size()); // Should be 2 tracks (1 updated, 1 added)

        // Verify track 101 was updated
        Optional<Track> updatedTrack101Opt = revertedProject.getTracks().stream().filter(t -> Long.valueOf(101L).equals(t.getId())).findFirst();
        assertTrue(updatedTrack101Opt.isPresent());
        assertEquals("Piano Updated", updatedTrack101Opt.get().getInstrument());
        assertEquals("seq1_updated", updatedTrack101Opt.get().getMusicalSequence());

        // Verify track 102 was deleted (Check repository interaction)
        verify(trackRepository, times(1)).deleteAll(trackListCaptor.capture());
        assertEquals(1, trackListCaptor.getValue().size());
        assertEquals(102L, trackListCaptor.getValue().get(0).getId());

        // Verify track 103 was added (it won't have an ID yet until saved by cascade)
        Optional<Track> addedTrackOpt = revertedProject.getTracks().stream().filter(t -> t.getId() == null && "Bass".equals(t.getInstrument())).findFirst();
        assertTrue(addedTrackOpt.isPresent());
        assertEquals("seq3_new", addedTrackOpt.get().getMusicalSequence());
        assertEquals(revertedProject, addedTrackOpt.get().getProject()); // Ensure it's linked

        // Verify save was called on the modified project
        verify(projectRepository, times(1)).save(projectCaptor.capture());
        assertEquals(project, projectCaptor.getValue()); // Check it's the same project instance that was modified

        verify(objectMapper, times(1)).readValue(sampleProjectJson, Project.class);
    }


    @Test
    @DisplayName("revertToVersion should throw ResourceNotFoundException if project not found")
    void revertToVersion_ProjectNotFound() throws JsonMappingException, JsonProcessingException {
        // Arrange
        Long projectId = 99L;
        Long versionId = 5L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectVersionService.revertToVersion(projectId, versionId, user);
        });
        verify(projectVersionRepository, never()).findById(any());
        verify(objectMapper, never()).readValue(anyString(), eq(Project.class));
    }

    @Test
    @DisplayName("revertToVersion should throw ResourceNotFoundException if version not found")
    void revertToVersion_VersionNotFound() throws JsonMappingException, JsonProcessingException {
        // Arrange
        Long projectId = 1L;
        Long versionId = 99L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectVersionRepository.findById(versionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectVersionService.revertToVersion(projectId, versionId, user);
        });
        verify(objectMapper, never()).readValue(anyString(), eq(Project.class));
    }

     @Test
     @DisplayName("revertToVersion should throw ResourceNotFoundException if version belongs to different project")
     void revertToVersion_VersionWrongProject() throws JsonMappingException, JsonProcessingException {
         // Arrange
         Long projectId = 1L;
         Long versionId = 5L;
         Project differentProject = Project.builder().id(2L).build(); // A different project
         ProjectVersion versionWithWrongProject = ProjectVersion.builder()
                 .id(versionId)
                 .project(differentProject) // Linked to the wrong project
                 .snapshotData(sampleProjectJson)
                 .build();

         when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
         when(projectVersionRepository.findById(versionId)).thenReturn(Optional.of(versionWithWrongProject));

         // Act & Assert
         // The exception occurs because the .filter() in the service method fails
         assertThrows(ResourceNotFoundException.class, () -> {
             projectVersionService.revertToVersion(projectId, versionId, user);
         });
         verify(objectMapper, never()).readValue(anyString(), eq(Project.class));
     }


    @Test
    @DisplayName("revertToVersion should throw RuntimeException on deserialization error")
    void revertToVersion_DeserializationError() throws JsonProcessingException {
        // Arrange
        Long projectId = 1L;
        Long versionId = 5L;
        ProjectVersion versionToRevertTo = ProjectVersion.builder()
                .id(versionId)
                .project(project)
                .snapshotData(sampleProjectJson)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectVersionRepository.findById(versionId)).thenReturn(Optional.of(versionToRevertTo));
        when(objectMapper.readValue(sampleProjectJson, Project.class)).thenThrow(new JsonProcessingException("Bad JSON") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            projectVersionService.revertToVersion(projectId, versionId, user);
        });
        assertEquals("Failed to deserialize snapshot for reverting", exception.getMessage());
        verify(projectRepository, never()).save(any()); // Ensure save wasn't called
    }

     // --- Tests for Revert Safety Flag (If Using Option A) ---
     // Note: Directly testing ThreadLocal manipulation in unit tests can be tricky.
     // These tests demonstrate the intent but might need adjustment or be better suited for integration tests.

     @Test
     @DisplayName("revertToVersion should set and clear isReverting flag (Conceptual Test)")
     @Disabled // Disable this test as directly testing static ThreadLocal from here is complex/fragile
     void revertToVersion_ManagesRevertingFlag() throws JsonProcessingException {
         // This test is hard to implement reliably at the unit level without more complex
         // setup (PowerMock for static methods or refactoring ProjectServiceImpl).
         // We focus on verifying the outcome (save call) instead.

         // --- Arrange ---
         Long projectId = 1L;
         Long versionId = 5L;
         ProjectVersion versionToRevertTo = ProjectVersion.builder()
                 .id(versionId).project(project).snapshotData(sampleProjectJson).build();

         when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
         when(projectVersionRepository.findById(versionId)).thenReturn(Optional.of(versionToRevertTo));
         when(objectMapper.readValue(sampleProjectJson, Project.class)).thenReturn(snapshotProject);
         when(projectRepository.save(any(Project.class))).thenReturn(project); // Return modified project

         // --- Setup state *before* calling revert ---
         ReflectionTestUtils.setField(ProjectServiceImpl.class, "isReverting", ThreadLocal.withInitial(() -> false)); // Reset if possible
         @SuppressWarnings("unchecked")
         ThreadLocal<Boolean> isReverting = (ThreadLocal<Boolean>) ReflectionTestUtils.getField(ProjectServiceImpl.class, "isReverting");
         assertNotNull(isReverting, "isReverting field should not be null");
         assertFalse(isReverting.get());


         // --- Act ---
         projectVersionService.revertToVersion(projectId, versionId, user);

         // --- Assert ---
         // Asserting the flag *during* the save call within revertToVersion is the tricky part.
         // Instead, we rely on the integration test or verify that if updateProject *were* called
         // during the revert, its snapshot logic would check a flag that *should* have been set.

         // Verify the state *after* calling revert
          Object isRevertingField = ReflectionTestUtils.getField(ProjectServiceImpl.class, "isReverting");
          assertNotNull(isRevertingField, "isReverting field should not be null");
          assertTrue(isRevertingField instanceof ThreadLocal, "isReverting field should be of type ThreadLocal<Boolean>");
          @SuppressWarnings("unchecked")
          ThreadLocal<Boolean> isRevertingField2 = (ThreadLocal<Boolean>) isRevertingField;
          assertFalse(isRevertingField2.get()); // Check it was reset

         // Verify the main save happened
         verify(projectRepository, times(1)).save(project);
     }
}