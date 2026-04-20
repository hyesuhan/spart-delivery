package com.sparta.spartadelivery.global.presentation.dto;

import java.util.List;

public record ApiResponse<T>(
        int status,
        String message,
        T data,
        List<ValidationErrorResponse> errors
) {

    private static final String SUCCESS_MESSAGE = "SUCCESS";

    public static <T> ApiResponse<T> success(int status, T data) {
        return success(status, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data, null);
    }

    public static ApiResponse<Void> error(int status, String message, List<ValidationErrorResponse> errors) {
        return new ApiResponse<>(status, message, null, errors);
    }
}
