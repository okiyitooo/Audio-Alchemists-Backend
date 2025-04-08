package com.kanaetochi.audio_alchemists.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.ProjectVersion;

@Repository
public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Long> {
    List<ProjectVersion> findByProjectIdOrderByTimeStampDesc(Long projectId); // Fetch versions in descending order of timestamp
}
