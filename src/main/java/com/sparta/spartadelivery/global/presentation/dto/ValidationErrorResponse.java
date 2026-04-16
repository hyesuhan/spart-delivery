package com.sparta.spartadelivery.global.presentation.dto;

public record ValidationErrorResponse(
        String field,
        String message
) {
}
