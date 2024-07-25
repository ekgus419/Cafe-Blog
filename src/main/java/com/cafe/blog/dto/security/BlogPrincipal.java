package com.cafe.blog.dto.security;

import com.cafe.blog.dto.UserAccountDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 스프링 시큐리티와 연동하여 사용자 인증 정보를 관리하기 위해 사용됩니다.
 * @param username
 * @param password
 * @param authorities
 * @param email
 * @param nickname
 * @param memo
 */
public record BlogPrincipal(
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        String email,
        String nickname,
        String memo
) implements UserDetails {

    public enum RoleType {
        USER("ROLE_USER");

        @Getter private final String name;

        RoleType(String name) {
            this.name = name;
        }
    }

    /**
     * of 팩토리 메소드: BlogPrincipal 객체를 생성합니다.
     * @param username
     * @param password
     * @param email
     * @param nickname
     * @param memo
     * @return
     */
    public static BlogPrincipal of(String username, String password, String email, String nickname, String memo) {
        // 지금은 인증만 하고 권한을 다루고 있지 않아서 임의로 세팅한다.
        Set<RoleType> roleTypes = Set.of(RoleType.USER);

        return new BlogPrincipal(
                username,
                password,
                roleTypes.stream()
                        .map(RoleType::getName)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet())
                ,
                email,
                nickname,
                memo
        );
    }

    /**
     * from 메소드: UserAccountDto를 BlogPrincipal로 변환합니다.
     * UserAccountDto 객체에서 BlogPrincipal 객체를 만들 때 사용합니다.
     * @param dto
     * @return
     */
    public static BlogPrincipal from(UserAccountDto dto) {
        return BlogPrincipal.of(
                dto.userId(),
                dto.userPassword(),
                dto.email(),
                dto.nickname(),
                dto.memo()
        );
    }

    /**
     * toDto 메소드: BlogPrincipal을 UserAccountDto로 변환합니다.
     * BlogPrincipal 객체를 UserAccountDto로 변환할 때 사용합니다.
     * @return UserAccountDto
     */
    public UserAccountDto toDto() {
        return UserAccountDto.of(
                username,
                password,
                email,
                nickname,
                memo
        );
    }

    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
