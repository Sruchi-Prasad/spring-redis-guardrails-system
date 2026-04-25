package com.assignment.controller;

import com.assignment.entity.Comment;
import com.assignment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{id}/comments")
    public Comment comment(@PathVariable Long id,
            @RequestBody Comment comment,
            @RequestParam(defaultValue = "false") boolean isBot) {
        return commentService.addComment(id, comment, comment.getAuthorId(), isBot);
    }

    @GetMapping("/{id}/comments")
    public List<Comment> getComments(@PathVariable Long id) {
        return commentService.getCommentsForPost(id);
    }
}
