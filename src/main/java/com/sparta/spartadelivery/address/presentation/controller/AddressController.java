package com.sparta.spartadelivery.address.presentation.controller;

import com.sparta.spartadelivery.address.application.AddressService;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressUpdateRequest;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressDetailInfo;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressInfo;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;


    @PostMapping
    public ResponseEntity<ApiResponse<AddressDetailInfo>> createdAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AddressCreateRequest request
            ) {

        AddressDetailInfo addressDetailInfo = addressService.createAddress(request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", addressDetailInfo));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressInfo>>> getMyAddresses(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        List<AddressInfo> addressInfos = addressService.getAddresses(userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", addressInfos));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> getAddressDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
            ) {

        AddressDetailInfo addressDetailInfo = addressService.getAddress(addressId, userPrincipal.getId());

        return  ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", addressDetailInfo));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId,
            @RequestBody AddressUpdateRequest request
    ) {

        AddressInfo addressInfo = addressService.updatedAddress(addressId, request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", addressInfo));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
    ) {

        addressService.deleteAddress(addressId, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "DELETED", null));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<ApiResponse<?>> setDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID addressId
    ) {

        addressService.changeDefaultAddress(addressId, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }


}
