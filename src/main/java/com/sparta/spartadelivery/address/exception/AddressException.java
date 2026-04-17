package com.sparta.spartadelivery.address.exception;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;

public class AddressException extends AppException {
    public AddressException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AddressException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
