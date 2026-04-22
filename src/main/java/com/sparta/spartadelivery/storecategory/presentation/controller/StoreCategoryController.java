package com.sparta.spartadelivery.storecategory.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.storecategory.application.service.StoreCategoryService;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryDetailResponse;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(
            summary = "가게 카테고리 목록 조회 API",
            description = """
                    가게 카테고리 목록을 페이지네이션으로 조회합니다.

                    **요청 가능 권한**

                    - ALL

                    **처리 정책**

                    - 삭제되지 않은 가게 카테고리만 조회할 수 있습니다.
                    - 기본 정렬은 createdAt,DESC 입니다.
                    - size는 10, 30, 50 중 하나만 사용할 수 있습니다.
                    - sort는 `{필드명},{정렬방향}` 형식으로 전달합니다.
                    - 정렬 가능 필드: `name`, `createdAt`, `updatedAt`
                    - 정렬 방향: `ASC`, `DESC`
                    - 예시: `name,ASC`, `createdAt,DESC`
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<StoreCategoryPageResponse>> getStoreCategories(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (허용값: 10, 30, 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "정렬 조건 (`{필드명},{정렬방향}` 형식, 허용 필드: name, createdAt, updatedAt / 방향: ASC, DESC)",
                    example = "createdAt,DESC"
            )
            @RequestParam(required = false) String sort
    ) {
        StoreCategoryPageResponse response = storeCategoryService.getStoreCategories(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
