package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.service.PlaceRecommendationService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place Recommendation", description = "공간 추천 관련 API")
@RestController
@RequestMapping("/api/places/recommendations")
@RequiredArgsConstructor
public class PlaceRecommendationController {

    private final PlaceRecommendationService placeRecommendationService;

    @Operation(
            summary = "주변 신규 공간 조회",
            description = "사용자 위치 기준, 같은 행정구역 내 최근 30일 생성된 공간을 거리순으로 조회합니다."
    )
    @GetMapping("/new")
    public ResponseEntity<List<PlaceSummaryResponse>> getNewPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam Long regionCode,
            @RequestParam PlaceCategory category,
            @RequestParam(required = false) Integer radiusMeters
    ) {
        List<PlaceSummaryResponse> places = placeRecommendationService.getNewPlaces(
                userDetails.userId(), longitude, latitude, regionCode, category, radiusMeters
        );
        return ResponseEntity.ok(places);
    }

    @Operation(summary = "비슷한 성향 공간 조회", description = "유저와 비슷한 성향의 공간을 추천합니다.")
    @GetMapping("/similar")
    public ResponseEntity<List<PlaceSummaryResponse>> getSimilarPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long regionCode,
            @RequestParam PlaceCategory category,
            @RequestParam double longitude,
            @RequestParam double latitude
    ) {
        List<PlaceSummaryResponse> places = placeRecommendationService.getSimilarPlaces(
                userDetails.userId(), regionCode, category, longitude, latitude
        );
        return ResponseEntity.ok(places);
    }

    @Operation(summary = "인기 공간 조회", description = "사용자 위치 기준, 반경 내 인기 순으로 공간을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PlaceSummaryResponse>> getPopularPlaces(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam PlaceCategory category,
            @RequestParam(required = false) Integer radiusMeters
    ) {
        List<PlaceSummaryResponse> places = placeRecommendationService.getPopularPlaces(
                userDetails.userId(), longitude, latitude, category, radiusMeters
        );
        return ResponseEntity.ok(places);
    }
}
