package com.sparta.spartadelivery.store.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.store.application.service.StoreService;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreCreateRequest;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreUpdateRequest;
import com.sparta.spartadelivery.store.presentation.dto.response.StoreDetailResponse;
import com.sparta.spartadelivery.store.presentation.dto.response.StorePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "가게 관리 API")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록 API")
    @PostMapping
    public ResponseEntity<ApiResponse<StoreDetailResponse>> createStore(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoreCreateRequest request
    ) {
        StoreDetailResponse response = storeService.createStore(request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));
    }

    @Operation(summary = "가게 수정 API")
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoreUpdateRequest request
    ) {
        StoreDetailResponse response = storeService.updateStore(storeId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "가게 숨김 처리 API")
    @PatchMapping("/{storeId}/hide")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> hideStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        StoreDetailResponse response = storeService.hideStore(storeId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "가게 목록 조회 API")
    @GetMapping
    public ResponseEntity<ApiResponse<StorePageResponse>> getStores(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (허용값 10, 30, 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    description = "정렬 조건 (`{필드명},{정렬방향}` 형식, 허용 필드: name, averageRating, createdAt, updatedAt / 방향: ASC, DESC)",
                    example = "createdAt,DESC"
            )
            @RequestParam(required = false) String sort
    ) {
        StorePageResponse response = storeService.getStores(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "가게 상세 조회 API")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStore(
            @PathVariable UUID storeId
    ) {
        StoreDetailResponse response = storeService.getStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(summary = "관리자용 가게 목록 조회 API")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<StorePageResponse>> getAdminStores(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (허용값 10, 30, 50)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "숨김 가게 포함 여부", example = "false")
            @RequestParam(defaultValue = "false") boolean hidden,
            @Parameter(
                    description = "정렬 조건 (`{필드명},{정렬방향}` 형식, 허용 필드: name, averageRating, createdAt, updatedAt / 방향: ASC, DESC)",
                    example = "createdAt,DESC"
            )
            @RequestParam(required = false) String sort
    ) {
        StorePageResponse response = storeService.getAdminStores(userPrincipal, page, size, sort, hidden);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
