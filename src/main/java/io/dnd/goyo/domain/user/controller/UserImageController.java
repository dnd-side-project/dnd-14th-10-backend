package io.dnd.goyo.domain.user.controller;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.image.ImageType;
import io.dnd.goyo.common.image.ImageUploadService;
import io.dnd.goyo.common.image.dto.request.PresignedUrlRequest;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Image", description = "유저 이미지 업로드 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users/images")
@RequiredArgsConstructor
public class UserImageController {

    private final ImageUploadService imageUploadService;

    @Operation(summary = "프로필 이미지 업로드용 Presigned URL 발급")
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrls(@Valid @RequestBody PresignedUrlRequest request) {
        if (request.filenames().size() > 1) {
            throw new BusinessException(ErrorCode.USER_IMAGE_LIMIT_EXCEEDED);
        }
        List<PresignedUrlItem> items = imageUploadService.createPresignedUrls(ImageType.USER, request.filenames());
        return ResponseEntity.ok(PresignedUrlResponse.from(items));
    }
}
