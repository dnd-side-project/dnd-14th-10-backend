package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.domain.place.service.PlaceImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place Image API", description = "장소 이미지 업로드 API")
@RestController
@RequestMapping("/api/places/images")
@RequiredArgsConstructor
public class PlaceImageController {

    private final PlaceImageService placeImageService;

    @Operation(summary = "이미지 업로드용 Presigned URL 발급", description = "장소 이미지를 업로드하기 위한 Presigned URL을 발급받습니다.")
    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam String filename) {
        String presignedUrl = placeImageService.createPresignedUrl(filename);
        return ResponseEntity.ok(Map.of("url", presignedUrl));
    }
}
