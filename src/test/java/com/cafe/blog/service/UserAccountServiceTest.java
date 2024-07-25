package com.cafe.blog.service;

import com.cafe.blog.dto.UserAccountDto;
import com.cafe.blog.dto.security.BlogPrincipal;
import com.cafe.blog.entity.UserAccount;
import com.cafe.blog.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Mockito 확장을 사용하여 테스트 클래스 확장
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;  // UserAccountRepository 목(Mock) 객체 생성

    @InjectMocks
    private UserAccountService userAccountService;  // UserAccountService 객체 생성 및 @Mock으로 생성된 객체 주입

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // PasswordEncoder 객체 생성

    // 회원 가입 테스트
    @Test
    void registerUser_shouldRegisterUser_whenUserDoesNotExist() {
        // given: 사용자 DTO 생성 및 저장될 사용자 엔티티 설정
        UserAccountDto userAccountDto = UserAccountDto.of("user1", "password", "user1@example.com", "nickname", "memo");
        UserAccount userAccount = UserAccount.of("user1", passwordEncoder.encode("password"), "user1@example.com", "nickname", "memo", "user1");
        given(userAccountRepository.existsById(userAccountDto.userId())).willReturn(false);  // 사용자 존재 여부 설정
        given(userAccountRepository.save(any(UserAccount.class))).willReturn(userAccount);  // 사용자 저장 설정

        // when: UserAccountService의 registerUser 메서드 호출
        UserAccountDto result = userAccountService.registerUser(userAccountDto);

        // then: 결과 검증
        assertNotNull(result);  // 결과가 null이 아닌지 확인
        assertEquals(userAccountDto.userId(), result.userId());  // 결과의 사용자 ID가 입력된 사용자 ID와 동일한지 확인
        verify(userAccountRepository).save(any(UserAccount.class));  // save 메서드가 호출되었는지 검증
    }

    // 회원 가입 실패 테스트: 이미 존재하는 사용자
    @Test
    void registerUser_shouldThrowException_whenUserAlreadyExists() {
        // given: 이미 존재하는 사용자 설정
        UserAccountDto userAccountDto = UserAccountDto.of("user1", "password", "user1@example.com", "nickname", "memo");
        given(userAccountRepository.existsById(userAccountDto.userId())).willReturn(true);  // 이미 존재하는 사용자 설정

        // when & then: UserAccountService의 registerUser 메서드를 호출했을 때, 예외 발생 검증
        assertThrows(IllegalArgumentException.class, () -> userAccountService.registerUser(userAccountDto));  // 예외 발생 확인
        verify(userAccountRepository, never()).save(any(UserAccount.class));  // save 메서드가 호출되지 않았는지 확인
    }

    // 회원 수정 테스트
    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        // given: 사용자 DTO 생성 및 조회될 사용자 엔티티 설정
        UserAccountDto userAccountDto = UserAccountDto.of("user1", "newpassword", "newemail@example.com", "newnickname", "newmemo");
        UserAccount userAccount = UserAccount.of("user1", passwordEncoder.encode("password"), "user1@example.com", "nickname", "memo", "user1");
        given(userAccountRepository.findById(userAccountDto.userId())).willReturn(Optional.of(userAccount));  // 사용자 조회 설정
        given(userAccountRepository.save(any(UserAccount.class))).willReturn(userAccount);  // 사용자 저장 설정

        // when: UserAccountService의 updateUser 메서드 호출
        BlogPrincipal principal = BlogPrincipal.of(userAccountDto.userId(), userAccountDto.userPassword(), userAccountDto.email(), userAccountDto.nickname(), userAccountDto.memo());  // BlogPrincipal 객체 생성
        UserAccountDto result = userAccountService.updateUser(userAccountDto.userId(), userAccountDto, principal);

        // then: 결과 검증
        assertNotNull(result);  // 결과가 null이 아닌지 확인
        assertEquals(userAccountDto.userId(), result.userId());  // 결과의 사용자 ID가 입력된 사용자 ID와 동일한지 확인
        verify(userAccountRepository).save(any(UserAccount.class));  // save 메서드가 호출되었는지 검증
    }

    // 회원 수정 실패 테스트: 존재하지 않는 사용자
    @Test
    void updateUser_shouldThrowException_whenUserDoesNotExist() {
        // given: 존재하지 않는 사용자 설정
        UserAccountDto userAccountDto = UserAccountDto.of("user1", "newpassword", "newemail@example.com", "newnickname", "newmemo");
        given(userAccountRepository.findById(userAccountDto.userId())).willReturn(Optional.empty());  // 존재하지 않는 사용자 설정

        // when & then: UserAccountService의 updateUser 메서드를 호출했을 때, 예외 발생 검증
        BlogPrincipal principal = BlogPrincipal.of(userAccountDto.userId(), userAccountDto.userPassword(), userAccountDto.email(), userAccountDto.nickname(), userAccountDto.memo());  // BlogPrincipal 객체 생성
        assertThrows(IllegalArgumentException.class, () -> userAccountService.updateUser(userAccountDto.userId(), userAccountDto, principal));  // 예외 발생 확인
        verify(userAccountRepository, never()).save(any(UserAccount.class));  // save 메서드가 호출되지 않았는지 확인
    }

    // 회원 삭제 테스트
    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        // given: 존재하는 사용자 설정
        UserAccount userAccount = UserAccount.of("user1", passwordEncoder.encode("password"), "user1@example.com", "nickname", "memo", "user1");
        given(userAccountRepository.findById("user1")).willReturn(Optional.of(userAccount));  // 존재하는 사용자 설정

        // when: UserAccountService의 deleteUser 메서드 호출
        BlogPrincipal principal = BlogPrincipal.of("user1", "password", "user1@example.com", "nickname", "memo");  // BlogPrincipal 객체 생성
        userAccountService.deleteUser("user1", principal);

        // then: 삭제 메서드 호출 검증
        verify(userAccountRepository).deleteById("user1");  // deleteById 메서드가 호출되었는지 검증
    }

    // 회원 삭제 실패 테스트: 존재하지 않는 사용자
    @Test
    void deleteUser_shouldThrowException_whenUserDoesNotExist() {
        // given: 존재하지 않는 사용자 설정
        given(userAccountRepository.findById("user1")).willReturn(Optional.empty());  // 존재하지 않는 사용자 설정

        // when & then: UserAccountService의 deleteUser 메서드를 호출했을 때, 예외 발생 검증
        BlogPrincipal principal = BlogPrincipal.of("user1", "password", "user1@example.com", "nickname", "memo");  // BlogPrincipal 객체 생성
        assertThrows(IllegalArgumentException.class, () -> userAccountService.deleteUser("user1", principal));  // 예외 발생 확인
        verify(userAccountRepository, never()).deleteById("user1");  // deleteById 메서드가 호출되지 않았는지 확인
    }
}
