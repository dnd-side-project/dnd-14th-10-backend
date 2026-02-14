package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.domain.place.dto.response.PlaceRegisterResponse;
import io.dnd.goyo.domain.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@Tag(name = "Place", description = "장소 관련 API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 제보(등록)", description = "새로운 장소를 제보합니다.")
    @PostMapping
    public ResponseEntity<PlaceRegisterResponse> registerPlace(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PlaceRegisterRequest request
    ) {
        Long placeId = placeService.registerPlace(userDetails.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaceRegisterResponse.from(placeId));
    }
}
