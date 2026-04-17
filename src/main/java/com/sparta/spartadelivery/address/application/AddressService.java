package com.sparta.spartadelivery.address.application;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressDetailInfo;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressInfo;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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


    // private methods
    private UserEntity getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST, "접근 권한이 없습니다."));
    }
}
