package com.sparta.spartadelivery.storecategory.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreCategoryErrorCode implements BaseErrorCode {
    // 가게 카테고리 등록 API 관련 에러 코드
    STORE_CATEGORY_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "가게 카테고리를 등록할 권한이 없습니다."),
    DUPLICATE_STORE_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 가게 카테고리명입니다.");

    private final HttpStatus status;
    private final String message;

    StoreCategoryErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
