package com.kanaetochi.audio_alchemists.service;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.User;
import java.util.Set; // Use Set for IDs

public interface FollowService {

    /**
     * Makes the followerUser follow the followingUser.
     * @param followerUser The user initiating the follow.
     * @param userIdToFollow The ID of the user to be followed.
     */
    void followUser(User followerUser, Long userIdToFollow);

    /**
     * Makes the followerUser unfollow the followingUser.
     * @param followerUser The user initiating the unfollow.
     * @param userIdToUnfollow The ID of the user to be unfollowed.
     * @throws ResourceNotFoundException if the follow relationship doesn't exist.
     */
    void unfollowUser(User followerUser, Long userIdToUnfollow);

    /**
     * Gets the IDs of users that the specified user is following.
     * @param userId The ID of the user whose following list is requested.
     * @return A Set of user IDs.
     */
    Set<Long> getFollowingIds(Long userId);

    /**
     * Gets the IDs of users who are following the specified user.
     * @param userId The ID of the user whose followers are requested.
     * @return A Set of user IDs.
     */
    Set<Long> getFollowerIds(Long userId); // Optional: Useful for displaying followers

    /**
     * Checks if user1 is following user2.
     * @param followerId The potential follower's ID.
     * @param followingId The potential followed user's ID.
     * @return true if followerId is following followingId, false otherwise.
     */
    boolean isFollowing(Long followerId, Long followingId);
}