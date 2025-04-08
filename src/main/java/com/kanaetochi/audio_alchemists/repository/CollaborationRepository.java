package com.kanaetochi.audio_alchemists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.Collaboration;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> { 
    List<Collaboration> findByProjectId(Long projectId);
    Optional<Collaboration> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    // Find project IDs a user collaborates on
    @Query("SELECT c.project.id FROM Collaboration c WHERE c.user.id = :userId")
    List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

    // Find user IDs collaborating on specific projects (excluding the user themselves)
    @Query("SELECT DISTINCT c.user.id FROM Collaboration c WHERE c.project.id IN :projectIds AND c.user.id <> :userId")
    List<Long> findCollaboratorIdsByProjectIdsExcludingUserId(
        @Param("projectIds") List<Long> projectIds,
        @Param("userId") Long userId
    );
}
