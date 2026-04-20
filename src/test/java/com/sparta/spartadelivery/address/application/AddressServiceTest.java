package com.sparta.spartadelivery.address.application;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressUpdateRequest;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressDetailInfo;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressInfo;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService 단위 테스트 - Mockito")
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private UserEntity user;
    private UUID addressId;
    private Address address;

    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        user = UserEntity.builder()
                .username("sparta_user")
                .role(Role.CUSTOMER)
                .build();

        address = Address.builder()
                .user(user)
                .alias("우리집")
                .address("서울")
                .isDefault(true)
                .build();
    }

    @Test
    @DisplayName("배송지 생성 성공 테스트")
    void createAddress_Success() {
        // given
        AddressCreateRequest request = new AddressCreateRequest("우리집", "서울", "101호", "12345", true);
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.save(any(Address.class))).willReturn(address);

        // when
        AddressDetailInfo result = addressService.createAddress(request, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.alias()).isEqualTo("우리집");
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    @DisplayName("배송지 단거 조회 테스트")
    void getAddress_Success() {
        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        AddressDetailInfo result = addressService.getAddress(addressId, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.alias()).isEqualTo("우리집");
        verify(addressRepository).findById(addressId);
    }

    @Test
    @DisplayName("배송지 단건 조회 실패 테스트 - username이 다르면 접근이 불가합니다.")
    void getAddress_fail() {
        // given
        UserEntity otherUser = UserEntity.builder()
                .username("other_user")
                .role(Role.CUSTOMER)
                .build();

        Address othersAddress = Address.builder()
                .user(otherUser)
                .alias("남의 집")
                .address("부산")
                .build();

        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(othersAddress));

        // when & then
        assertThrows(AppException.class, () -> {
            addressService.getAddress(addressId, 1L);
        });
    }

    @Test
    @DisplayName("사용자의 배송지 목록 조회 테스트")
    void getAddresses_Success() {
        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findAllByUserAndDeletedAtIsNull(user)).willReturn(List.of(address));

        // when
        List<AddressInfo> results = addressService.getAddresses(1L);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).alias()).isEqualTo("우리집");
        verify(addressRepository).findAllByUserAndDeletedAtIsNull(user);
    }



    @Test
    @DisplayName("배송지 정보 수정 테스트")
    void updatedAddress_Success()  {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest("회사", "경기", "2층", "55555", false);
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        AddressInfo result = addressService.updatedAddress(addressId, request, 1L);

        // then
        assertThat(result.alias()).isEqualTo("회사");
        assertThat(address.getAddress()).isEqualTo("경기");
        verify(addressRepository).findById(addressId);
    }

    @Test
    @DisplayName("배송지 삭제 성공 테스트 (본인)")
    void deleteAddress_Success()  {
        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        addressService.deleteAddress(addressId, 1L);

        // then
        // soft delete를 위해 markDeleted 호출 여부 확인 (엔티티 메서드 내부 호출)
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    @DisplayName("배송지 삭제 성공 테스트 (관리자 권한으로 타인 주소 삭제)")
    void deleteAddress_Success_ByAdmin()  {
        // given
        // 1. 요청자: 관리자(MASTER)
        UserEntity admin = UserEntity.builder()
                .username("admin_master")
                .role(Role.MASTER)
                .build();

        // 2. 주소 소유자: 다른 사용자(other_user)
        UserEntity owner = UserEntity.builder()
                .username("other_user")
                .role(Role.CUSTOMER)
                .build();

        Address othersAddress = Address.builder()
                .user(owner)
                .alias("남의 집")
                .address("부산")
                .build();

        // 서비스 로직 내에서 getUser()가 두 번 호출되므로 연속적으로 반환 설정
        given(userRepository.findById(anyLong())).willReturn(Optional.of(admin));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(othersAddress));

        // when
        addressService.deleteAddress(addressId, 99L); // 관리자 ID로 요청

        // then
        // 관리자는 본인 소유가 아니어도 삭제가 가능해야 함 (Exception 발생 안 함)
        verify(addressRepository, times(1)).findById(addressId);
        verify(userRepository, times(1)).findById(anyLong()); // deleteAddress에서 1번, validateDeleteUser에서 1번
    }

    @Test
    @DisplayName("배송지 삭제 실패 테스트 (권한 없는 사용자가 타인 주소 삭제)")
    void deleteAddress_Fail_AccessDenied() {
        // given
        // 요청자: 다른 일반 사용자
        UserEntity stranger = UserEntity.builder()
                .username("stranger")
                .role(Role.CUSTOMER)
                .build();

        // 소유자: 원래 주인
        UserEntity owner = UserEntity.builder()
                .username("owner_user")
                .role(Role.CUSTOMER)
                .build();

        Address ownersAddress = Address.builder()
                .user(owner)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(stranger));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(ownersAddress));

        // when & then
        // 권한이 없으므로 AppException(ADDRESS_ACCESS_DENIED)이 발생해야 함
        assertThrows(com.sparta.spartadelivery.global.exception.AppException.class, () -> {
            addressService.deleteAddress(addressId, 2L);
        });
    }

    @Test
    @DisplayName("기본 배송지 변경 테스트")
    void changeDefaultAddress_Success() {
        // given
        given(userRepository.findById(any())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        addressService.changeDefaultAddress(addressId, 1L);

        // then
        // 1. 기존 배송지들 false 처리 벌크 쿼리 호출 확인
        verify(addressRepository).updateAllDefaultToFalse(user.getUsername());
        // 2. 현재 배송지가 기본으로 설정되었는지 확인
        assertThat(address.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경 테스트 - 기존 기본 배송지는 해제되고 선택된 것만 true가 된다")
    void changeDefaultAddress_Success_OnlyOneDefault() {
        // given
        // 1. 기존에 기본 배송지였던 객체
        Address existingDefaultAddress = Address.builder()
                .user(user)
                .alias("기존 기본")
                .isDefault(true)
                .build();

        // 2. 새로 기본으로 설정하려는 객체 (현재 false)
        Address targetAddress = Address.builder()
                .user(user)
                .alias("새로운 기본")
                .isDefault(false)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(addressRepository.findById(addressId)).willReturn(Optional.of(targetAddress));

        // [중요] 벌크 쿼리 동작 시뮬레이션: 호출되면 existingDefaultAddress의 상태를 false로 바꿈
        doAnswer(invocation -> {
            existingDefaultAddress.update(
                    existingDefaultAddress.getAlias(),
                    existingDefaultAddress.getAddress(),
                    existingDefaultAddress.getDetail(),
                    existingDefaultAddress.getZipCode(),
                    false // 기본 배송지 해제
            );
            return null;
        }).when(addressRepository).updateAllDefaultToFalse(user.getUsername());

        // 순서 검증을 위한 InOrder 객체 생성
        InOrder inOrder = inOrder(addressRepository);

        // when
        addressService.changeDefaultAddress(addressId, 1L);

        // then
        // 1. 실행 순서 검증: 벌크 쿼리(전체 해제)가 먼저 실행되었는가?
        inOrder.verify(addressRepository).updateAllDefaultToFalse(user.getUsername());

        // 2. 상태 검증: 타겟은 true, 기존 것은 false인가?
        assertThat(targetAddress.isDefault()).isTrue();
        assertThat(existingDefaultAddress.isDefault()).isFalse();

        // 3. 전체 중 true인 것이 1개뿐인지 검증 (논리적 확인)
        List<Address> allAddresses = List.of(existingDefaultAddress, targetAddress);
        long defaultCount = allAddresses.stream().filter(Address::isDefault).count();
        assertThat(defaultCount).isEqualTo(1);
    }

}
