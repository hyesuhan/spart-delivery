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

    @Operation(
            summary = "가게 등록 API",
            description = """
                    새로운 가게를 등록합니다.

                    **요청 가능 권한**

                    - OWNER

                    **처리 정책**

                    - 로그인한 OWNER 사용자를 가게 소유자로 저장합니다.
                    - 삭제되지 않은 가게 카테고리와 지역만 참조할 수 있습니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<StoreDetailResponse>> createStore(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoreCreateRequest request
    ) {
        StoreDetailResponse response = storeService.createStore(request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));
    }

    @Operation(
            summary = "가게 수정 API",
            description = """
                    기존 가게 정보를 수정합니다.

                    **요청 가능 권한**

                    - OWNER (본인 가게만 가능)
                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 삭제되지 않은 가게만 수정할 수 있습니다.
                    - 삭제되지 않은 가게 카테고리와 지역만 수정값으로 사용할 수 있습니다.
                    - OWNER는 본인 소유 가게만 수정할 수 있습니다.
                    """
    )
    @PutMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StoreUpdateRequest request
    ) {
        StoreDetailResponse response = storeService.updateStore(storeId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "가게 목록 조회 API",
            description = """
                    가게 목록을 페이지네이션 형태로 조회합니다.

                    **요청 가능 권한**

                    - ALL

                    **처리 정책**

                    - 삭제되지 않고 숨김 처리되지 않은 가게만 조회합니다.
                    - 기본 정렬은 createdAt,DESC 입니다.
                    - size는 10, 30, 50 중 하나만 사용할 수 있습니다.
                    - sort는 `{필드명},{정렬방향}` 형식으로 전달합니다.
                    - 정렬 가능 필드: `name`, `averageRating`, `createdAt`, `updatedAt`
                    - 정렬 방향: `ASC`, `DESC`
                    """
    )
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

    @Operation(
            summary = "관리자용 가게 목록 조회 API",
            description = """
                    관리자/운영자용 가게 목록을 페이지네이션 형태로 조회합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 삭제되지 않은 가게를 조회합니다.
                    - `hidden=true`이면 숨김 가게를 포함해 조회합니다.
                    - `hidden=false`이면 숨김 처리되지 않은 가게만 조회합니다.
                    - 기본 정렬은 createdAt,DESC 입니다.
                    - size는 10, 30, 50 중 하나만 사용할 수 있습니다.
                    - sort는 `{필드명},{정렬방향}` 형식으로 전달합니다.
                    - 정렬 가능 필드: `name`, `averageRating`, `createdAt`, `updatedAt`
                    - 정렬 방향: `ASC`, `DESC`
                    """
    )
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
