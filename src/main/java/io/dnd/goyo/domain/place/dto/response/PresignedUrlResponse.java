package io.dnd.goyo.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponse(
        @Schema(description = "업로드용 Presigned URL", example = "http://minio:9000/bucket/uuid.jpg?...")
        String url
) {
    public static PresignedUrlResponse from(String url) {
        return new PresignedUrlResponse(url);
    }
}
