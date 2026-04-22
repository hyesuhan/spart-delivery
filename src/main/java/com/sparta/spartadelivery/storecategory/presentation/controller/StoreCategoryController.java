package com.sparta.spartadelivery.storecategory.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.storecategory.application.service.StoreCategoryService;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/store-categories")
@RequiredArgsConstructor
@Tag(name = "StoreCategory", description = "가게 카테고리 관리 API")
public class StoreCategoryController {

    private final StoreCategoryService storeCategoryService;

    @Operation(
            summary = "가게 카테고리 등록 API",
            description = """
                    새로운 가게 카테고리를 등록합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 삭제되지 않은 가게 카테고리 중 같은 이름이 있으면 등록할 수 없습니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<StoreCategoryDetailResponse>> createStoreCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoreCategoryCreateRequest request
    ) {
        StoreCategoryDetailResponse response = storeCategoryService.createStoreCategory(request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));
    }
}
