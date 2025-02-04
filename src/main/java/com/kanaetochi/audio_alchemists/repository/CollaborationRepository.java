package com.kanaetochi.audio_alchemists.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.Collaboration;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> { 
    List<Collaboration> findByProjectId(Long projectId);
    Optional<Collaboration> findByProjectIdAndUserId(Long projectId, Long userId);
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
