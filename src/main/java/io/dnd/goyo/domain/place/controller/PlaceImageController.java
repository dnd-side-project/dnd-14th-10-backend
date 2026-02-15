package io.dnd.goyo.domain.place.controller;

import io.dnd.goyo.common.image.ImageType;
import io.dnd.goyo.common.image.ImageUploadService;
import io.dnd.goyo.common.image.dto.request.PresignedUrlRequest;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place Image", description = "장소 이미지 업로드 API")
@RestController
@RequestMapping("/api/places/images")
@RequiredArgsConstructor
public class PlaceImageController {

    private final ImageUploadService imageUploadService;

    @Operation(summary = "이미지 업로드용 Presigned URL 발급")
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrls(@Valid @RequestBody PresignedUrlRequest request) {
        List<PresignedUrlItem> items = imageUploadService.createPresignedUrls(ImageType.PLACE, request.filenames());
        return ResponseEntity.ok(PresignedUrlResponse.from(items));
    }
}
