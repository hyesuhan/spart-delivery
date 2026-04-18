package com.sparta.spartadelivery.user.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    // 유일성과 불변성을 만족하지 못하는 username 대신 user_id를 PK로 사용한다.
    // username은 정책이나 상황에 따라 변경될 수 있으며, 동명이인이 존재할 가능성이 있다.
    private Long id;

    @Column(name = "username", length = 10, nullable = false)
    private String username;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    // 로그인 시 사용되는 로그인 ID
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    // TODO: 복수 권한 부여가 필요해지면 단일 Role 필드를 Set<Role> 구조로 전환한다.
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Builder
    private UserEntity(
            String username,
            String nickname,
            String email,
            String password,
            Role role,
            boolean isPublic
    ) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isPublic = isPublic;
    }

    public void updateProfile(String username, String nickname, String email, Boolean isPublic) {
        if (username != null) {
            this.username = username;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (email != null) {
            this.email = email;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
