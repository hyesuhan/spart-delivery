package com.sparta.spartadelivery.address.exception;

import com.sparta.spartadelivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AddressErrorCode {

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_NOT_FOUND"),
    ADDRESS_


    private final HttpStatus status;
    private final String message;

}
