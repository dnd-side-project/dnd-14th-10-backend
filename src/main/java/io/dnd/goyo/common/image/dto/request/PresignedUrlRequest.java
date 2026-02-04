package io.dnd.goyo.common.image.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Presigned URL 발급 요청")
public record PresignedUrlRequest(
        @Schema(description = "업로드할 파일명 목록", example = "[\"image1.jpg\", \"image2.png\"]")
        @NotEmpty(message = "파일명 목록은 필수입니다")
        @Size(max = 10, message = "이미지는 한 번에 최대 10장까지 업로드할 수 있습니다")
        List<@NotBlank(message = "파일명은 비어있을 수 없습니다") String> filenames
) {}
