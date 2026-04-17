package com.sparta.spartadelivery.address.presentation.controller;

import com.sparta.spartadelivery.address.application.AddressService;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createdAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody createdAddressRequest request
            ) {
        //  TODO: 현재 로그인한 유저 정보로 주소지 만들기
return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getMyAddresses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // TODO: 현재 로그인한 유저 정보로 모든 주소지 가져오기
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> getAddressDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
            ) {
        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId,
            @RequestBody updateAddressRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "DELETED", null));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<?>> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }
}
