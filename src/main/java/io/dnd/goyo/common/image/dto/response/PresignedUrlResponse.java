package io.dnd.goyo.common.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PresignedUrlResponse(
        @Schema(description = "Presigned URL 목록")
        List<PresignedUrlItem> urls
) {
    public static PresignedUrlResponse from(List<PresignedUrlItem> urls) {
        return new PresignedUrlResponse(urls);
    }

    public record PresignedUrlItem(
            @Schema(description = "원본 파일명", example = "cat.jpg")
            String filename,

            @Schema(description = "업로드용 Presigned URL", example = "http://minio:9000/bucket/uuid.jpg?...")
            String url,

            @Schema(description = "저장된 객체 키 (DB 저장용)", example = "place/uuid.jpg")
            String objectKey
    ) {
        public static PresignedUrlItem of(String filename, String url, String objectKey) {
            return new PresignedUrlItem(filename, url, objectKey);
        }
    }
}
