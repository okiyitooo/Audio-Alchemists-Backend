package com.kanaetochi.audio_alchemists.service.impl;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Follow;
import com.kanaetochi.audio_alchemists.model.FollowId;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.repository.FollowRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
import com.kanaetochi.audio_alchemists.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void followUser(User followerUser, Long userIdToFollow) {
        if (Objects.equals(followerUser.getId(), userIdToFollow)) {
            throw new IllegalArgumentException("User cannot follow themselves.");
        }

        User userToFollow = userRepository.findById(userIdToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userIdToFollow));

        FollowId followId = new FollowId(followerUser.getId(), userIdToFollow);
        if (followRepository.existsById(followId)) {
             log.warn("User {} already follows user {}", followerUser.getId(), userIdToFollow);
             throw new IllegalStateException("Already following this user.");
        }

        Follow follow = Follow.builder()
                .follower(followerUser)
                .following(userToFollow)
                .build();

        followRepository.save(follow);
        log.info("User {} started following user {}", followerUser.getId(), userIdToFollow);
    }

    @Override
    @Transactional
    public void unfollowUser(User followerUser, Long userIdToUnfollow) {
         if (Objects.equals(followerUser.getId(), userIdToUnfollow)) {
            throw new IllegalArgumentException("User cannot unfollow themselves.");
        }

        FollowId followId = new FollowId(followerUser.getId(), userIdToUnfollow);

        // Check if the follow relationship exists before trying to delete
        if (!followRepository.existsById(followId)) {
             log.warn("Follow relationship not found for user {} unfollowing user {}", followerUser.getId(), userIdToUnfollow);
             throw new ResourceNotFoundException("Follow", "followerId/followingId", followerUser.getId() + "/" + userIdToUnfollow);
        }

        followRepository.deleteById(followId);
        log.info("User {} unfollowed user {}", followerUser.getId(), userIdToUnfollow);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIdsByFollowerId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFollowerIds(Long userId) {
        // Need to add findFollowerIdsByFollowingId to FollowRepository
        return followRepository.findFollowerIdsByFollowingId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsById(new FollowId(followerId, followingId));
    }
}