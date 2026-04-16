package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final Long id;
    // accountName은 userEntity의 userName을 나타낸다.
    // UserDetails에 getUsername라는 부모 메서드가 이미 존재하므로 accountName이라고 명명한다.
    private final String accountName;
    private final String password;
    private final String nickname;
    private final String email;
    private final Role role;

    public static UserPrincipal from(UserEntity user) {
        return UserPrincipal.builder()
                .id(user.getId())
                .accountName(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.asAuthority()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    // 로그인 ID로 이메일을 사용한다.
    @Override
    public String getUsername() {
        return email;
    }
}
