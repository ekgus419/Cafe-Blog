package com.cafe.blog.service;

import com.cafe.blog.dto.PostDto;
import com.cafe.blog.dto.UserAccountDto;
import com.cafe.blog.entity.Post;
import com.cafe.blog.entity.UserAccount;
import com.cafe.blog.exception.PostNotFoundException;
import com.cafe.blog.repository.PostRepository;
import com.cafe.blog.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private MultipartFile file;

    private final String testUploadDir = "test/upload/dir";  // 테스트에서 사용할 임의의 업로드 경로

    @BeforeEach
    void setUp() {
        // 업로드 경로를 테스트에서 사용하는 디렉토리로 설정
        postService.setUploadDir(testUploadDir);

        UserAccount userAccount = UserAccount.of("user1", "password", "user1@example.com", "nickname", "memo");
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        PostDto postDto = PostDto.of(
                UserAccountDto.from(userAccount),
                "Test Title",
                "Test Content",
                null,
                null,
                null
        );

        // 기타 초기화 작업
    }

    @Test
    void createPost_shouldSavePost_whenPostDtoIsValidAndFileIsProvided() throws IOException {
        // given
        UserAccount userAccount = UserAccount.of("user1", "password", "user1@example.com", "nickname", "memo");
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        PostDto postDto = PostDto.of(
                UserAccountDto.from(userAccount),
                "Test Title",
                "Test Content",
                "testfile.txt",
                testUploadDir + "/testfile.txt",
                "text/plain"
        );

        given(userAccountRepository.getReferenceById(any())).willReturn(userAccount);
        given(postRepository.save(any(Post.class))).willReturn(post);
        given(file.isEmpty()).willReturn(false);  // 파일이 비어있지 않다고 설정
        given(file.getOriginalFilename()).willReturn("testfile.txt");
        given(file.getContentType()).willReturn("text/plain");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(), any())).thenAnswer(invocation -> null);

            // when
            postService.createPost(postDto, file);

            // then
            verify(userAccountRepository).getReferenceById(userAccount.getUserId());
            verify(postRepository).save(any(Post.class));
        }
    }

    @Test
    void updatePost_shouldUpdatePost_whenPostExistsAndFileIsProvided() throws IOException {
        // given
        UserAccount userAccount = UserAccount.of("user1", "password", "user1@example.com", "nickname", "memo");
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        PostDto updatedPostDto = PostDto.of(
                UserAccountDto.from(userAccount),
                "Updated Title",
                "Updated Content",
                "updatedfile.txt",
                testUploadDir + "/updatedfile.txt",
                "text/plain"
        );

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post));
        given(postRepository.save(any(Post.class))).willReturn(post);
        given(file.isEmpty()).willReturn(false);  // 파일이 비어있지 않다고 설정
        given(file.getOriginalFilename()).willReturn("updatedfile.txt");
        given(file.getContentType()).willReturn("text/plain");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenAnswer(invocation -> null);
            mockedFiles.when(() -> Files.copy(any(), any())).thenAnswer(invocation -> null);

            // when
            PostDto result = postService.updatePost(1L, updatedPostDto, file);

            // then
            assertThat(result.title()).isEqualTo("Updated Title");
            verify(postRepository).findById(1L);
            verify(postRepository).save(any(Post.class));
        }
    }

    @Test
    public void deletePost_shouldDeletePost_whenPostExists() throws IOException {
        // Given
        Long postId = 1L;
        String filePath = "path/to/file.txt";
        UserAccount userAccount = UserAccount.of("user1", "password", "user1@example.com", "nickname", "memo");
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        post.setFilePath(filePath);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        // Mocking static method Files.deleteIfExists (requires Mockito 3.4.0 or higher)
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(Paths.get(filePath))).thenReturn(true);

            // When
            postService.deletePost(postId);

            // Then
            verify(postRepository).deleteById(postId);
            mockedFiles.verify(() -> Files.deleteIfExists(Paths.get(filePath)));
        }
    }


    @Test
    void deletePost_shouldThrowException_whenPostDoesNotExist() {
        // given
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(PostNotFoundException.class, () -> postService.deletePost(1L));
        verify(postRepository, never()).deleteById(1L);
    }
}
