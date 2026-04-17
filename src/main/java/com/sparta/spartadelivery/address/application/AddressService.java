package com.sparta.spartadelivery.address.application;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.address.exception.AddressException;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressUpdateRequest;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressDetailInfo;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressInfo;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional
    public AddressDetailInfo createAddress(AddressCreateRequest request, Long userId) {

        UserEntity user = getUser(userId);

        Address address = AddressCreateRequest.of(user, request);

        return AddressDetailInfo.fromEntity(addressRepository.save(address));
    }

    public List<AddressInfo> getAddresses(Long userId) {
        UserEntity user = getUser(userId);

        return addressRepository.finAllByUserDeletedAtIsNull(user)
                .stream().map(AddressInfo::of).toList();

    }

    public AddressDetailInfo getAddress(UUID addressId, Long userId) throws AccessDeniedException {
        UserEntity user = getUser(userId);

        Address address = findAndValidateAddress(addressId, user);

        return AddressDetailInfo.fromEntity(address);
    }

    @Transactional
    public AddressInfo updatedAddress(UUID addressId, AddressUpdateRequest request, Long userId) throws AccessDeniedException {
        UserEntity user = getUser(userId);

        Address address = findAndValidateAddress(addressId, user);

        address.update(request.alias(), request.address(),request.detail(), request.zipCode(),request.isDefault());

        return AddressInfo.of(address);
    }

    @Transactional
    public void deleteAddress(UUID addressId, Long userId) throws AccessDeniedException {
        UserEntity user = getUser(userId);

        Address address = findAndValidateAddress(addressId, user);

        address.markDeleted(user.getUsername());
    }


    // private methods
    private UserEntity getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "접근 권한이 없습니다."));
    }

    private Address findAndValidateAddress(UUID addressId, UserEntity user) throws AccessDeniedException {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "주소를 찾을 수 없습니다."));

        if (!address.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("해당 접근 권한이 없습니다.");
        }

        return  address;
    }


}
