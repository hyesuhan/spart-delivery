package com.sparta.spartadelivery.area.presentation.controller;

import com.sparta.spartadelivery.area.application.service.AreaService;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaUpdateRequest;
import com.sparta.spartadelivery.area.presentation.dto.response.AreaDetailResponse;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
@Tag(name = "Area", description = "운영 지역 관리 API")
public class AreaController {

    private final AreaService areaService;

    @Operation(
            summary = "운영 지역 등록 API",
            description = """
                    새로운 운영 지역을 등록합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 삭제되지 않은 운영 지역 중 같은 지역명이 있으면 등록할 수 없습니다.
                    - active 값을 생략하면 기본값 true로 등록됩니다.
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<AreaDetailResponse>> createArea(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AreaCreateRequest request
    ) {
        AreaDetailResponse response = areaService.createArea(request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));
    }

    @Operation(
            summary = "운영 지역 수정 API",
            description = """
                    운영 지역 정보를 수정합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 삭제되지 않은 운영 지역만 수정할 수 있습니다.
                    - 지역명이 변경되는 경우 삭제되지 않은 운영 지역 기준으로 중복 검증을 수행합니다.
                    """
    )
    @PutMapping("/{areaId}")
    public ResponseEntity<ApiResponse<AreaDetailResponse>> updateArea(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID areaId,
            @Valid @RequestBody AreaUpdateRequest request
    ) {
        AreaDetailResponse response = areaService.updateArea(areaId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "운영 지역 삭제 API",
            description = """
                    운영 지역을 soft delete 방식으로 삭제 처리합니다.

                    **요청 가능 권한**

                    - MASTER

                    **처리 정책**

                    - 실제 데이터를 삭제하지 않고 deletedAt, deletedBy를 기록합니다.
                    - 이미 삭제된 운영 지역은 삭제 대상으로 조회되지 않습니다.
                    """
    )
    @DeleteMapping("/{areaId}")
    public ResponseEntity<ApiResponse<Void>> deleteArea(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID areaId
    ) {
        areaService.deleteArea(areaId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }
}
