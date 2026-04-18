package com.sparta.spartadelivery.user.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements BaseErrorCode {
    USER_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "사용자 정보 수정 권한이 없습니다."),
    MANAGER_TARGET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MANAGER는 CUSTOMER 또는 OWNER 사용자 정보만 수정할 수 있습니다."),
    MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD(HttpStatus.FORBIDDEN, "MANAGER와 MASTER는 사용자의 비밀번호를 수정할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
