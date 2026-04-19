package com.sparta.spartadelivery.address.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AddressErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    INVALID_ADDRESS_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 주소 형식입니다."),
    MISSING_ADDRESS_DETAIL(HttpStatus.BAD_REQUEST, "필수 주소 정보가 누락되었습니다."),
    NOT_SERVICEABLE_AREA(HttpStatus.BAD_REQUEST, "서비스 불가능 지역입니다."),
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 등록 가능한 주소 개수를 초과했습니다."),

    ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 주소에 대한 접근 권한이 없습니다."),

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 주소 입니다."),

    ADDRESS_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주소 조회 중 오류가 발생했습니다.")

    ;



    private final HttpStatus status;
    private final String message;


    AddressErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
