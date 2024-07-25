package com.cafe.blog.repository;

import com.cafe.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 제목으로 검색
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 내용으로 검색
    Page<Post> findByContentContainingIgnoreCase(String content, Pageable pageable);

    // 유저 아이디로 검색
    Page<Post> findByUserAccount_UserId(String userId, Pageable pageable);

}
