package com.kanaetochi.audio_alchemists.service;

import java.util.List;

import com.kanaetochi.audio_alchemists.model.Comment;

public interface CommentService {
    Comment createComment(Comment comment, Long projectId, Long userId);
    List<Comment> getAllCommentsByProject(Long projectId);
    void deleteComment(Long id);
}
