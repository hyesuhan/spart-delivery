package com.sparta.spartadelivery.area.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AreaErrorCode implements BaseErrorCode {
    // 운영 지역 등록 API 관련 에러 코드
    AREA_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "운영 지역을 등록할 권한이 없습니다."),
    DUPLICATE_AREA_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 운영 지역명입니다."),

    // 운영 지역 목록 조회 API 관련 에러 코드
    AREA_LIST_INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다."),
    AREA_LIST_INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 10, 30, 50 중 하나여야 합니다."),
    AREA_LIST_INVALID_SORT_FORMAT(HttpStatus.BAD_REQUEST, "정렬 조건은 {property},{direction} 형식이어야 합니다."),
    AREA_LIST_UNSUPPORTED_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다."),
    AREA_LIST_UNSUPPORTED_SORT_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 사용할 수 있습니다."),

    // 운영 지역 수정 API 관련 에러 코드
    AREA_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "운영 지역을 수정할 권한이 없습니다."),

    // 운영 지역 삭제 API 관련 에러 코드
    AREA_DELETE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "운영 지역을 삭제할 권한이 없습니다."),
    AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "운영 지역을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    AreaErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
