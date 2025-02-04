package com.kanaetochi.audio_alchemists.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kanaetochi.audio_alchemists.exception.ResourceNotFoundException;
import com.kanaetochi.audio_alchemists.model.Comment;
import com.kanaetochi.audio_alchemists.repository.CommentRepository;
import com.kanaetochi.audio_alchemists.repository.ProjectRepository;
import com.kanaetochi.audio_alchemists.repository.UserRepository;
import com.kanaetochi.audio_alchemists.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public Comment createComment(Comment comment, Long projectId, Long userId) {
        comment.setProject(projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId)));
        comment.setUser(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
        return commentRepository.save(comment);
    }

    @Override   
    public void deleteComment(Long id) {
        commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        commentRepository.deleteById(id);
    }

    @Override
    public List<Comment> getAllCommentsByProject(Long projectId) {
        return commentRepository.findAll().stream().filter(comment -> comment.getProject().getId().equals(projectId)).toList();
    }
}
