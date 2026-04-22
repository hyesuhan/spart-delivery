package com.sparta.spartadelivery.storecategory.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreCategoryErrorCode implements BaseErrorCode {
    // 가게 카테고리 등록 API 관련 에러 코드
    STORE_CATEGORY_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "가게 카테고리를 등록할 권한이 없습니다."),
    DUPLICATE_STORE_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 가게 카테고리명입니다."),

    // 가게 카테고리 목록 조회 API 관련 에러 코드
    STORE_CATEGORY_LIST_INVALID_PAGE_NUMBER(HttpStatus.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다."),
    STORE_CATEGORY_LIST_INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 10, 30, 50 중 하나여야 합니다."),
    STORE_CATEGORY_LIST_INVALID_SORT_FORMAT(HttpStatus.BAD_REQUEST, "정렬 조건은 {property},{direction} 형식이어야 합니다."),
    STORE_CATEGORY_LIST_UNSUPPORTED_SORT_PROPERTY(HttpStatus.BAD_REQUEST, "지원하지 않는 정렬 필드입니다."),
    STORE_CATEGORY_LIST_UNSUPPORTED_SORT_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 사용할 수 있습니다."),

    // 가게 카테고리 상세 조회 API 관련 에러 코드
    STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "가게 카테고리를 찾을 수 없습니다."),

    // 가게 카테고리 수정 API 관련 에러 코드
    STORE_CATEGORY_UPDATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "가게 카테고리를 수정할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;

    StoreCategoryErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
