package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceUpdateRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceRegisterResponse;
import io.dnd.goyo.domain.place.service.PlaceService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place", description = "공간 관련 API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "공간 제보(등록)", description = "새로운 공간을 제보합니다.")
    @PostMapping
    public ResponseEntity<PlaceRegisterResponse> registerPlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PlaceRegisterRequest request
    ) {
        Long placeId = placeService.registerPlace(userDetails.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceRegisterResponse.from(placeId));
    }

    @Operation(summary = "공간 상세 조회", description = "공간의 상세 정보를 조회합니다.")
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDetailResponse> getPlaceDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId
    ) {
        Long userId = getUserId(userDetails);
        PlaceDetailResponse response = placeService.getPlaceDetail(userId, placeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공간 수정", description = "등록한 공간 정보를 수정합니다.")
    @PatchMapping("/{placeId}")
    public ResponseEntity<Void> updatePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId,
            @Valid @RequestBody PlaceUpdateRequest request
    ) {
        placeService.updatePlace(userDetails.userId(), placeId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공간 삭제", description = "등록한 공간을 삭제합니다.")
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long placeId
    ) {
        placeService.deletePlace(userDetails.userId(), placeId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.userId() : null;
    }
}
