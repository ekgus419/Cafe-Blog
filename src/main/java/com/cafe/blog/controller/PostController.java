package com.cafe.blog.controller;

import com.cafe.blog.dto.PostDto;
import com.cafe.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/posts")
@Validated
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")  // 인증된 사용자만 접근 가능
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody PostDto postDto,
            @RequestParam(required = false) MultipartFile file) throws IOException {
        PostDto createdPost = postService.createPost(postDto, file);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")  // 인증된 사용자만 접근 가능
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostDto postDto,
            @RequestParam(required = false) MultipartFile file) throws IOException {
        PostDto updatedPost = postService.updatePost(postId, postDto, file);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")  // 인증된 사용자만 접근 가능
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) throws IOException {
        postService.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
