package com.cafe.blog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
@Table(indexes = {
        @Index(columnList = "title"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy")
})
@Entity
public class Post extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @JoinColumn(name = "userId")
    @ManyToOne(optional = false)
    private UserAccount userAccount; // 유저 정보 (ID)

    @Column(length = 500, nullable = false)
    @Setter private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @Setter private String content;

    @Column(length = 255)
    @Setter private String fileName;

    @Column(length = 2048) // 파일 경로 길이에 따라 조정
    @Setter private String filePath;

    @Column(length = 255)
    @Setter private String fileType;

    public Post withFileDetails(String fileName, String filePath, String fileType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        return this;
    }

    /**
     * JPA 는 기본 생성자가 필요 -> protected 로 설정하여 외부에서 인스턴스를 직접 생성하지 못하게 하고
     * JPA 내부에서만 사용할 수 있게 합니다.
     */
    protected Post() {
    }

    private Post(UserAccount userAccount, String title, String content) {
        this.userAccount = userAccount;
        this.title = title;
        this.content = content;
    }

    /**
     * of 메소드는 팩토리 메소드 패턴을 사용하여 객체 생성 로직을 캡슐화합니다.
     * new 키워드를 사용하여 객체를 직접 생성하는 대신 of 메소드를 사용하면
     * 코드의 가독성과 유지보수성이 향상됩니다.
     * Post 객체를 생성할 때 new Post(...) 대신 Post.of(...)를 사용합니다.
     * Post post = Post.of(userAccount, "Test Title", "Test Content");
     * @param userAccount
     * @param title
     * @param content
     * @return Post
     */
    public static Post of(UserAccount userAccount, String title, String content) {
        return new Post(userAccount, title, content);
    }

}
