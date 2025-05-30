package com.kanaetochi.audio_alchemists.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kanaetochi.audio_alchemists.dto.CommentDto;
import com.kanaetochi.audio_alchemists.dto.CommentMessage;
import com.kanaetochi.audio_alchemists.model.Comment;
import com.kanaetochi.audio_alchemists.model.User;
import com.kanaetochi.audio_alchemists.service.CommentService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("/projects/{projectId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    final private SimpMessagingTemplate template;
    private final ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long projectId, @RequestBody Comment comment, @AuthenticationPrincipal User autheticatedUser) {
        if (autheticatedUser == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = autheticatedUser.getId();
        Comment newComment = commentService.createComment(comment, projectId, userId);
        sendCommentMessage(newComment, projectId, userId);
        CommentDto commentDto = modelMapper.map(newComment, CommentDto.class);
        return ResponseEntity.ok(commentDto);
    }
    @GetMapping
    public ResponseEntity<?> getAllComments(@PathVariable Long projectId) {
        List<Comment> comments = commentService.getAllCommentsByProject(projectId);
        List<CommentDto> commentDtos = comments.stream().map(comment -> modelMapper.map(comment, CommentDto.class)).toList();
        return ResponseEntity.ok(commentDtos);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }
    
    private void sendCommentMessage(Comment comment, Long projectId, Long userId) {
        CommentMessage commentMessage = CommentMessage.builder()
            .text(comment.getText())
            .projectId(projectId)
            .userId(userId)
            .build();
        template.convertAndSend("/topic/project/" + projectId, commentMessage);
    }
}
