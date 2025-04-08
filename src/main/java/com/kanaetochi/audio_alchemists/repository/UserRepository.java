package com.kanaetochi.audio_alchemists.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Find users by their IDs, excluding certain IDs
    List<User> findByIdIn(List<Long> sharedCollaboratorIds, Pageable pageable);
    List<User> findByIdInAndIdNotIn(List<Long> sharedCollaboratorIds, Set<Long> excludeUserIds, Pageable pageable);
}
