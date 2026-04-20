package com.sparta.spartadelivery.user.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements BaseErrorCode {
    // 사용자 상세 조회 API 관련 에러 코드
    USER_DETAIL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "다른 사용자 정보 상세 조회 권한이 없습니다."),

    // 사용자 목록 조회 API 관련 에러 코드
    USER_LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "사용자 목록 조회 권한이 없습니다."),
    USER_LIST_INVALID_SORT_FORMAT(HttpStatus.BAD_REQUEST, "정렬 조건은 property,direction 형식이어야 합니다."),
    USER_LIST_UNSUPPORTED_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 사용자 목록 정렬 필드입니다."),
    USER_LIST_UNSUPPORTED_SORT_DIRECTION(HttpStatus.BAD_REQUEST, "사용자 목록 정렬 방향은 ASC 또는 DESC만 사용할 수 있습니다."),
    USER_LIST_INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "사용자 목록 페이지 번호는 0 이상이어야 합니다."),
    USER_LIST_INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "사용자 목록 페이지 크기는 10, 30, 50만 사용할 수 있습니다."),

    // 사용자 정보 수정 API 관련 에러 코드
    USER_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "사용자 정보 수정 권한이 없습니다."),
    MANAGER_TARGET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MANAGER는 CUSTOMER 또는 OWNER 사용자 정보만 수정할 수 있습니다."),
    MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD(HttpStatus.FORBIDDEN, "MANAGER와 MASTER는 사용자의 비밀번호를 수정할 수 없습니다."),

    // 사용자 탈퇴 API 관련 에러 코드
    MASTER_DELETE_DENIED(HttpStatus.FORBIDDEN, "MASTER 계정은 탈퇴할 수 없습니다."),

    // 사용자 권한 수정 API 관련 에러 코드
    USER_ROLE_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "사용자 권한 수정 권한이 없습니다."),
    SELF_ROLE_UPDATE_DENIED(HttpStatus.FORBIDDEN, "자기 자신의 권한은 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
