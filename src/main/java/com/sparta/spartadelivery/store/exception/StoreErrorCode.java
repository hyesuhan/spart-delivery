package com.sparta.spartadelivery.store.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreErrorCode implements BaseErrorCode {
    STORE_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "가게를 등록할 권한이 없습니다."),
    STORE_CREATE_OWNER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "가게는 OWNER 권한 사용자만 등록할 수 있습니다."),
    STORE_ADMIN_LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "관리자용 가게 목록을 조회할 권한이 없습니다."),
    STORE_LIST_INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다."),
    STORE_LIST_INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 10, 30, 50 중 하나여야 합니다."),
    STORE_LIST_INVALID_SORT_FORMAT(HttpStatus.BAD_REQUEST, "정렬 조건은 {property},{direction} 형식이어야 합니다."),
    STORE_LIST_UNSUPPORTED_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다."),
    STORE_LIST_UNSUPPORTED_SORT_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 사용할 수 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    StoreErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
