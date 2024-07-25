package com.cafe.blog.service;

import com.cafe.blog.dto.UserAccountDto;
import com.cafe.blog.dto.security.BlogPrincipal;
import com.cafe.blog.entity.UserAccount;
import com.cafe.blog.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional(readOnly = true)
    public Optional<UserAccountDto> searchUser(String username) {
        return userAccountRepository.findById(username)
                .map(UserAccountDto::from);
    }

    // 회원 가입
    public UserAccountDto registerUser(UserAccountDto userAccountDto) {
        // 사용자가 이미 존재하는지 확인
        if (userAccountRepository.existsById(userAccountDto.userId())) {
            throw new IllegalArgumentException("User already exists");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userAccountDto.userPassword());

        // UserAccount 엔티티 생성
        UserAccount userAccount = UserAccount.of(
                userAccountDto.userId(),
                encodedPassword,
                userAccountDto.email(),
                userAccountDto.nickname(),
                userAccountDto.memo(),
                userAccountDto.userId() // 최초 생성자는 생성자와 동일
        );

        // UserAccount 저장
        UserAccount savedUser = userAccountRepository.save(userAccount);
        return UserAccountDto.from(savedUser);
    }

    // 회원 수정
    @PreAuthorize("hasRole('ROLE_ADMIN') or #principal.username == #userAccountDto.userId")
    public UserAccountDto updateUser(String userId, UserAccountDto userAccountDto, BlogPrincipal principal) {
        // UserAccount 조회
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userAccountDto.userPassword());

        // UserAccount 업데이트
        userAccount.setUserPassword(encodedPassword);
        userAccount.setEmail(userAccountDto.email());
        userAccount.setNickname(userAccountDto.nickname());
        userAccount.setMemo(userAccountDto.memo());
        // JPA Auditing이 수정자 설정을 자동으로 처리합니다

        // UserAccount 저장
        UserAccount updatedUser = userAccountRepository.save(userAccount);
        return UserAccountDto.from(updatedUser);
    }

    // 회원 삭제
    @PreAuthorize("hasRole('ROLE_ADMIN') or #principal.username == #userId")
    public void deleteUser(String userId, BlogPrincipal principal) {
        // 사용자 존재 여부 확인
        userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 사용자 삭제
        userAccountRepository.deleteById(userId);
    }
}
