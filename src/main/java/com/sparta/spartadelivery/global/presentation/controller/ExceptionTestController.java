package com.sparta.spartadelivery.global.presentation.controller;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/exceptions")
public class ExceptionTestController {

    @GetMapping("/app")
    public void appException() {
        throw new AppException(GlobalErrorCode.INVALID_REQUEST, "AppException test");
    }

    @GetMapping("/runtime")
    public void runtimeException() {
        throw new RuntimeException("RuntimeException test");
    }

    @PostMapping("/validation")
    public void validationException(@Valid @RequestBody ValidationTestRequest request) {
        // 이 엔드포인트는 컨트롤러 로직이 실행되기 전에 @Valid 검증을 트리거하기 위해서만 존재합니다.
    }

    public record ValidationTestRequest(
            @NotBlank(message = "name is required")
            String name
    ) {
    }
}
