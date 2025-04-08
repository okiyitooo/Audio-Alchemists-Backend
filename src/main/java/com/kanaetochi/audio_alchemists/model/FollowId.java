package com.kanaetochi.audio_alchemists.model;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class FollowId implements Serializable {

    private static final long serialVersionUID = 1L; 

    private Long follower; // Corresponds to the 'follower' field (User ID) in Follow entity
    private Long following; // Corresponds to the 'following' field (User ID) in Follow entity

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowId followId = (FollowId) o;
        return Objects.equals(follower, followId.follower) && Objects.equals(following, followId.following);
    }

    @Override
    public int hashCode() {
        return Objects.hash(follower, following);
    }
}