package com.cafe.blog.service;

import com.cafe.blog.dto.PostDto;
import com.cafe.blog.entity.Post;
import com.cafe.blog.entity.UserAccount;
import com.cafe.blog.entity.constant.SearchType;
import com.cafe.blog.exception.PostNotFoundException;
import com.cafe.blog.repository.PostRepository;
import com.cafe.blog.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;

    @Setter
    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;  // 파일 저장 경로

    @Transactional(readOnly = true)
    public Page<PostDto> searchPost(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return postRepository.findAll(pageable).map(PostDto::from);
        }

        return switch (searchType) {
            case TITLE -> postRepository.findByTitleContainingIgnoreCase(searchKeyword, pageable).map(PostDto::from);
            case CONTENT -> postRepository.findByContentContainingIgnoreCase(searchKeyword, pageable).map(PostDto::from);
            case ID -> postRepository.findByUserAccount_UserId(searchKeyword, pageable).map(PostDto::from);
        };
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long postId) {
        return postRepository.findById(postId)
                .map(PostDto::from)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 없습니다 - postId: " + postId));
    }

    public PostDto createPost(PostDto postDto, MultipartFile file) throws IOException {
        UserAccount userAccount = userAccountRepository.getReferenceById(postDto.userAccountDto().userId());
        Post post = postDto.toEntity(userAccount);

        // 파일 저장
        if (file != null && !file.isEmpty()) {
            String fileName = saveFile(file);
            post.setFileName(fileName);
            post.setFilePath(Paths.get(uploadDir).resolve(fileName).toString());
            post.setFileType(file.getContentType());
        }

        postRepository.save(post);
        return PostDto.from(post);  // 반환하는 객체
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PostDto updatePost(Long postId, PostDto postDto, MultipartFile file) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 게시글 수정
        post.setTitle(postDto.title());
        post.setContent(postDto.content());

        // 파일 처리
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            if (post.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(post.getFilePath()));
            }

            // 새 파일 저장
            String fileName = saveFile(file);
            post.setFileName(fileName);
            post.setFilePath(Paths.get(uploadDir).resolve(fileName).toString());
            post.setFileType(file.getContentType());
        }

        postRepository.save(post);
        return PostDto.from(post);  // 반환하는 객체
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deletePost(Long postId) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post with id " + postId + " not found."));

        // 파일 삭제
        if (post.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(post.getFilePath()));
        }

        postRepository.deleteById(postId);
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        // 파일 저장 위치 경로 가져오기
        Path locationPath = Paths.get(uploadDir);

        // 경로가 존재하지 않으면 디렉토리 생성
        if (!Files.exists(locationPath)) {
            Files.createDirectories(locationPath);
        }

        // 저장할 파일의 전체 경로 생성
        Path filePath = locationPath.resolve(file.getOriginalFilename());

        // 파일을 지정된 경로에 저장
        Files.copy(file.getInputStream(), filePath);

        return file.getOriginalFilename();  // 저장된 파일의 이름 반환
    }

}
