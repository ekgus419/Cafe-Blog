package com.cafe.blog.repository;

import com.cafe.blog.entity.Post;
import com.cafe.blog.entity.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({PostRepositoryTests.TestJpaConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")  // Use the 'test' profile to avoid conflicts with dev/prod environments
class PostRepositoryTests {

    @EnableJpaAuditing
    @TestConfiguration
    static class TestJpaConfig {
        @Bean
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("test");
        }
    }

    private final PostRepository postRepository;

    private final UserAccountRepository userAccountRepository;

    PostRepositoryTests(@Autowired PostRepository postRepository, @Autowired UserAccountRepository userAccountRepository) {
        this.postRepository = postRepository;
        this.userAccountRepository = userAccountRepository;
    }

    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        // Create and save a user account for testing
        userAccount = UserAccount.of("testUser", "password", "test@example.com", "nickname", "memo");
        userAccount = userAccountRepository.save(userAccount);
    }

    @Test
    @DisplayName("Create Post Test")
    void givenPost_whenSave_thenGetOk() {
        // given
        Post post = Post.of(userAccount, "Test Title", "Test Content");

        // when
        Post savedPost = postRepository.save(post);

        // then
        assertThat(savedPost).isNotNull();
        assertThat(savedPost.getId()).isNotNull();
        assertThat(savedPost.getTitle()).isEqualTo("Test Title");
        assertThat(savedPost.getContent()).isEqualTo("Test Content");
        assertThat(savedPost.getUserAccount()).isEqualTo(userAccount);
    }

    @Test
    @DisplayName("Read Post Test")
    void givenPost_whenFindById_thenGetOk() {
        // given
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        Post savedPost = postRepository.save(post);

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("Update Post Test")
    void givenPost_whenUpdate_thenGetUpdatedOk() {
        // given
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        Post savedPost = postRepository.save(post);

        // when
        savedPost.setTitle("Updated Title");
        Post updatedPost = postRepository.save(savedPost);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("Delete Post Test")
    void givenPost_whenDelete_thenRemoved() {
        // given
        Post post = Post.of(userAccount, "Test Title", "Test Content");
        Post savedPost = postRepository.save(post);
        Long postId = savedPost.getId();

        // when
        postRepository.deleteById(postId);
        Optional<Post> deletedPost = postRepository.findById(postId);

        // then
        assertThat(deletedPost).isNotPresent();
    }
}
