package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.domain.place.dto.request.NearbyFilterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceUpdateRequest;
import io.dnd.goyo.domain.place.dto.response.MyPlaceResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceFilterResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceMapItemResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceRegisterResponse;
import io.dnd.goyo.domain.place.enums.PlaceSortType;
import io.dnd.goyo.domain.place.service.PlaceSearchService;
import io.dnd.goyo.domain.place.service.PlaceService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place", description = "공간 관련 API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceSearchService placeSearchService;

    @Operation(summary = "공간 필터 검색", description = "카테고리, 분위기, 공간 크기, 행정구역으로 공간을 검색합니다. (거리순 정렬)\n\n※ 필터 조건 변경 시 lastDistance를 초기화해야 합니다.")
    @GetMapping("/search")
    public ResponseEntity<PlaceFilterResponse> searchPlaces(
            @ParameterObject @Valid @ModelAttribute PlaceFilterRequest request
    ) {
        PlaceFilterResponse response = placeSearchService.getFilteredPlaces(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "반경 내 공간 필터 검색", description = "현재 위치 기준 반경 내 공간을 필터 검색합니다. (거리순 정렬)\n\n※ 필터 조건 변경 시 lastDistance를 초기화해야 합니다.")
    @GetMapping("/search/nearby")
    public ResponseEntity<PlaceFilterResponse> searchNearbyPlaces(
            @ParameterObject @Valid @ModelAttribute NearbyFilterRequest request
    ) {
        PlaceFilterResponse response = placeSearchService.getNearbyFilteredPlaces(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 등록 공간 조회", description = "로그인한 사용자가 등록한 공간 목록을 페이지네이션으로 조회합니다. 정렬은 sortType 파라미터를 사용하세요.")
    @GetMapping("/me")
    public ResponseEntity<Page<MyPlaceResponse>> getMyPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호(page)와 크기(size)만 사용. sort는 sortType 파라미터로 대체됩니다.")
            @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "정렬 조건: LATEST(최신순), NAME(이름순), POPULAR(인기순)")
            @RequestParam(defaultValue = "LATEST") PlaceSortType sortType
    ) {
        return ResponseEntity.ok(placeService.getMyPlaces(userDetails.userId(), pageable, sortType));
    }

    @Operation(summary = "공간 일괄 조회", description = "ID 목록으로 공간을 일괄 조회합니다.")
    @GetMapping("/batch")
    public ResponseEntity<List<PlaceMapItemResponse>> getPlacesByIds(
            @RequestParam @Size(max = 100) List<Long> ids
    ) {
        return ResponseEntity.ok(placeService.getPlacesByIds(ids));
    }

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
