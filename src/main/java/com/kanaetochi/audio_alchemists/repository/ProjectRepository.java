package com.kanaetochi.audio_alchemists.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Project> searchProjects(@Param("query") String query);
    
    // This is a simplified approach assuming genre is a simple string field for now. 
    // Adapt if using JSONB complex queries
    @Query("SELECT p FROM Project p WHERE p.genre IN :genres AND p.id NOT IN :excludedProjectIds AND p.owner.id <> :userId")
    List<Project> findProjectsByGenreAndExclude(
            @Param("genres") List<String> genres,
            @Param("excludedProjectIds") List<Long> excludedProjectIds,
            @Param("userId") Long userId,
            Pageable pageable);
    
    // Find projects owned by specific users, excluding specific project IDs
    @Query("SELECT p FROM Project p WHERE p.owner.id IN :ownerIds AND p.id NOT IN :excludedProjectIds")
    List<Project> findProjectsByOwnerIdsAndExclude(
        @Param("ownerIds") List<Long> ownerIds,
        @Param("excludedProjectIds") List<Long> excludedProjectIds,
        Pageable pageable
    );

    List<Long> findIdsByOwnerId(Long ownerId); 
}
