package com.assignment.controller;

import com.assignment.entity.Post;
import com.assignment.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService service;

    @GetMapping
    public List<Post> listAll() {
        return service.getAllPosts();
    }

    @GetMapping("/{id}")
    public Optional<Post> getOne(@PathVariable Long id) {
        return service.getPostById(id);
    }

    @PostMapping
    public Post create(@RequestBody Post post) {
        return service.createPost(post);
    }

    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id,
            @RequestParam Long userId) {
        service.likePost(id, userId);
        return "Liked!";
    }

    @GetMapping("/{id}/virality")
    public Long getVirality(@PathVariable Long id) {
        return service.getViralityScore(id);
    }
}
