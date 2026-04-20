package com.sparta.spartadelivery.address.domain.repository;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("AddressRepository 테스트")
@Import(AddressRepositoryTest.AuditConfig.class)
public class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity user;

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditConfig {
        @Bean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of("TEST_USER"); // BaseEntity의 createdBy 결측 방지
        }
    }

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 및 저장
        user = UserEntity.builder()
                .username("user1")
                .nickname("nick1")
                .email("test@sparta.com")
                .password("password")
                .role(Role.CUSTOMER)
                .isPublic(true)
                .build();
        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    @DisplayName("사용자의 삭제되지 않은 배송지 목록 조회 테스트")
    void findAllByUserDeletedAtIsNull_Success() {
        // given
        Address address1 = Address.builder()
                .user(user)
                .address("서울시 강남구")
                .zipCode("12345")
                .isDefault(true)
                .build();

        entityManager.persist(address1);
        entityManager.flush();

        // when
        List<Address> results = addressRepository.findAllByUserAndDeletedAtIsNull(user);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAddress()).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("기존 기본 배송지를 모두 해제(false) 처리하는 벌크 쿼리 테스트")
    void updateAllDefaultToFalse_Success() {
        // given
        Address address1 = Address.builder()
                .user(user)
                .address("기존 주소")
                .isDefault(true)
                .build();
        entityManager.persist(address1);
        entityManager.flush();

        // when
        addressRepository.updateAllDefaultToFalse(user.getUsername());
        // 벌크 연산 후 영속성 컨텍스트가 자동으로 비워짐 (clearAutomatically = true)

        // then
        Address updatedAddress1 = addressRepository.findById(address1.getId()).orElseThrow();
        assertThat(updatedAddress1.isDefault()).isFalse();
    }
}
