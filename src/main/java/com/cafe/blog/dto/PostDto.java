package com.cafe.blog.dto;

import com.cafe.blog.entity.Post;
import com.cafe.blog.entity.UserAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import java.time.LocalDateTime;

public record PostDto (
        Long id,

        @NotNull(message = "User account information cannot be null")
        UserAccountDto userAccountDto,

        @NotBlank(message = "Title cannot be blank")
        @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
        String title,

        @NotBlank(message = "Content cannot be blank")
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy,
        String fileName,
        String filePath,
        String fileType
){

    /**
     * 다양한 입력 데이터로 PostDto 객체를 생성하기 위해 사용합니다.
     * 필요한 필드에 대해 오버로드된 of 메소드를 제공함으로써 다양한 상황에서 PostDto 객체를 쉽게 생성할 수 있습니다.
     * @param userAccountDto
     * @param title
     * @param content
     * @return
     */
    public static PostDto of(UserAccountDto userAccountDto, String title, String content, String fileName, String filePath, String fileType) {
        return new PostDto(null, userAccountDto, title, content, null, null, null, null, fileName, filePath, fileType);
    }

    public static PostDto of(Long id, UserAccountDto userAccountDto, String title, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy, String fileName, String filePath, String fileType) {
        return new PostDto(id, userAccountDto, title, content, createdAt, createdBy, modifiedAt, modifiedBy, fileName, filePath, fileType);
    }


    /**
     * Post 엔티티를 PostDto로 변환하기 위해 사용합니다. 이 메소드는 엔티티에서 DTO로의 변환을 담당합니다.
     * 데이터베이스에서 가져온 Post 엔티티를 PostDto로 변환할 때 사용합니다.
     * @param postEntity
     * @return
     */
    public static PostDto from(Post postEntity) {
        return new PostDto(
                postEntity.getId(),
                UserAccountDto.from(postEntity.getUserAccount()),
                postEntity.getTitle(),
                postEntity.getContent(),
                postEntity.getCreatedAt(),
                postEntity.getCreatedBy(),
                postEntity.getModifiedAt(),
                postEntity.getModifiedBy(),
                postEntity.getFileName(),
                postEntity.getFilePath(),
                postEntity.getFileType()
        );
    }

    /**
     * PostDto 객체를 Post 엔티티로 변환하기 위해 사용합니다. 이 메소드는 DTO에서 엔티티로의 변환을 담당합니다.
     * PostDto 객체를 데이터베이스에 저장하거나 업데이트하기 위해 Post 엔티티로 변환할 때 사용합니다.
     * @param userAccount
     * @return
     */
    public Post toEntity(UserAccount userAccount) {
        return Post.of(
                userAccount,
                title,
                content
        ).withFileDetails(fileName, filePath, fileType); // 파일 관련 필드 설정;
    }

}
