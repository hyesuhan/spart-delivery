package com.sparta.spartadelivery.menu.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MenuErrorCode implements BaseErrorCode {
    // 메뉴 조회 관련 에러 코드
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    MENU_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 메뉴에 접근할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    MenuErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
