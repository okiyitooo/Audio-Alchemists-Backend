package com.kanaetochi.audio_alchemists.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
@Builder
@IdClass(FollowId.class) // Specify the composite key class
public class Follow {

    @Id // Mark as part of the composite primary key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false) // Foreign key column in 'follow' table
    private User follower;

    @Id // Mark as part of the composite primary key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false) // Foreign key column in 'follow' table
    private User following;

    @Column(name = "followed_at", nullable = false, updatable = false) // Track when follow happened
    private LocalDateTime followedAt;

    @PrePersist
    protected void onCreate() {
        followedAt = LocalDateTime.now();
    }
}