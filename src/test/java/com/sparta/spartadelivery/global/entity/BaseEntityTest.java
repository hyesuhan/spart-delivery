package com.sparta.spartadelivery.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.spartadelivery.global.infrastructure.config.JpaAuditingConfig;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

// JPA auditing 설정을 함께 로드해서 BaseEntity 필드가 영속화 과정에서 채워지는지 확인한다.
@DataJpaTest
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BaseEntityTest {

    private static final String TEST_AUDITOR = "base-entity-test-user";

    @Autowired
    private BaseEntityTestRepository repository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("엔티티 저장 시 로그인 사용자 기준으로 생성/수정 감사 정보가 기록된다")
    void saveWithAuthenticatedUser() {
        setAuthentication(TEST_AUDITOR);
        BaseEntityTestEntity entity = new BaseEntityTestEntity("first-name");

        BaseEntityTestEntity savedEntity = repository.saveAndFlush(entity);

        assertThat(savedEntity.getCreatedAt()).isNotNull();
        assertThat(savedEntity.getUpdatedAt()).isNotNull();
        assertThat(savedEntity.getCreatedBy()).isEqualTo(TEST_AUDITOR);
        assertThat(savedEntity.getUpdatedBy()).isEqualTo(TEST_AUDITOR);
        assertThat(savedEntity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("인증 정보가 없으면 SYSTEM으로 생성/수정 감사 정보가 기록된다")
    void saveWithoutAuthentication() {
        BaseEntityTestEntity entity = new BaseEntityTestEntity("system-name");

        BaseEntityTestEntity savedEntity = repository.saveAndFlush(entity);

        assertThat(savedEntity.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(savedEntity.getUpdatedBy()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("엔티티 수정 시 수정 시간이 갱신된다")
    void update() throws InterruptedException {
        setAuthentication(TEST_AUDITOR);
        BaseEntityTestEntity savedEntity = repository.saveAndFlush(new BaseEntityTestEntity("before-name"));
        var createdAt = savedEntity.getCreatedAt();
        var beforeUpdatedAt = savedEntity.getUpdatedAt();

        Thread.sleep(10);
        savedEntity.updateName("after-name");
        BaseEntityTestEntity updatedEntity = repository.saveAndFlush(savedEntity);

        assertThat(updatedEntity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updatedEntity.getUpdatedAt()).isAfter(beforeUpdatedAt);
        assertThat(updatedEntity.getUpdatedBy()).isEqualTo(TEST_AUDITOR);
    }

    @Test
    @DisplayName("markDeleted 호출 시 삭제 상태와 삭제자가 기록된다")
    void markDeleted() {
        BaseEntityTestEntity savedEntity = repository.saveAndFlush(new BaseEntityTestEntity("delete-name"));

        savedEntity.markDeleted("delete-user");

        assertThat(savedEntity.isDeleted()).isTrue();
        assertThat(savedEntity.getDeletedAt()).isNotNull();
        assertThat(savedEntity.getDeletedBy()).isEqualTo("delete-user");
    }

    private void setAuthentication(String accountName) {
        UserPrincipal principal = UserPrincipal.builder()
                .accountName(accountName)
                .password("password")
                .nickname("nickname")
                .email("test@example.com")
                .role(Role.CUSTOMER)
                .build();
        var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
