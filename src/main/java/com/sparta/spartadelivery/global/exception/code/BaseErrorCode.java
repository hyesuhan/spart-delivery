package com.sparta.spartadelivery.global.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    String getMessage();
    HttpStatus getStatus();
}
