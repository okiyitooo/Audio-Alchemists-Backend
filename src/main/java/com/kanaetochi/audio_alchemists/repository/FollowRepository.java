package com.kanaetochi.audio_alchemists.repository;

import com.kanaetochi.audio_alchemists.model.Follow;
import com.kanaetochi.audio_alchemists.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    // Find users the given user is following
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    Set<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId);

    // Find users followed by a list of users (for follows-of-follows)
    @Query("SELECT DISTINCT f.following.id FROM Follow f WHERE f.follower.id IN :followerIds")
    Set<Long> findFollowingIdsByFollowerIds(@Param("followerIds") Set<Long> followerIds); // Use Set for input as well

    // Find users following the given user
    @Query("SELECT f.follower.id FROM Follow f WHERE f.following.id = :userId")
    Set<Long> findFollowerIdsByFollowingId(@Param("userId") Long userId);

    // Find users followed by a list of users (for follows-of-follows)
    @Query("SELECT DISTINCT f.follower.id FROM Follow f WHERE f.following.id IN :followingIds")
    Set<Long> findFollowerIdsByFollowingIds(@Param("followingIds") Set<Long> followingIds); // Use Set for input
}