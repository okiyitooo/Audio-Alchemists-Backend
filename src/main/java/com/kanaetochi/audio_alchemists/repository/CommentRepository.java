package com.kanaetochi.audio_alchemists.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kanaetochi.audio_alchemists.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
}
