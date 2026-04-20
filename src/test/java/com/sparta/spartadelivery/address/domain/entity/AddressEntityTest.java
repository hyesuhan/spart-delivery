package com.sparta.spartadelivery.address.domain.entity;

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AddressEntityTest {

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .username("testUser")
                .nickname("테스터")
                .email("test@sparta.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .isPublic(true)
                .build();
    }

    @Test
    @DisplayName("Address 생성 성공 테스트")
    void createAddress_Success() {
        // given
        String alias = "집";
        String addressLine = "서울시 강남구 테헤란로";
        String detail = "101호";
        String zipCode = "12345";

        // when
        Address address = Address.builder()
                .user(user)
                .alias(alias)
                .address(addressLine)
                .detail(detail)
                .zipCode(zipCode)
                .isDefault(false)
                .build();

        // then
        assertThat(address).isNotNull();
        assertThat(address.getUser()).isEqualTo(user);
        assertThat(address.getAlias()).isEqualTo(alias);
        assertThat(address.getAddress()).isEqualTo(addressLine);
        assertThat(address.isDefault()).isFalse();
    }

    @Test
    @DisplayName("Address 정보 수정 테스트")
    void updateAddress_Success() {
        // given
        Address address = Address.builder()
                .user(user)
                .alias("기존")
                .address("기존 주소")
                .isDefault(false)
                .build();

        // when
        address.update("회사", "경기도 성남시", "2층", "54321", true);

        // then
        assertThat(address.getAlias()).isEqualTo("회사");
        assertThat(address.getAddress()).isEqualTo("경기도 성남시");
        assertThat(address.getDetail()).isEqualTo("2층");
        assertThat(address.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 설정(setAsDefault) 검증")
    void setAsDefault_Success() {
        // given
        Address address = Address.builder()
                .user(user)
                .address("서울")
                .isDefault(false)
                .build();

        // when
        address.setAsDefault();

        // then
        assertThat(address.isDefault()).isTrue();
    }

}
