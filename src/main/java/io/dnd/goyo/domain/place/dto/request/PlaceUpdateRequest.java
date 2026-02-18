package io.dnd.goyo.domain.place.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.List;


@Schema(description = "공간 수정 요청")
public record PlaceUpdateRequest(
        @Schema(description = "공간 이름", example = "고작 아지트 강남점")
        String name,

        @Schema(description = "층수", example = "2")
        Integer floorInfo,

        @Schema(description = "영업 시작 시간 (HH:mm)", example = "10:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @Schema(description = "영업 종료 시간 (HH:mm)", example = "02:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,

        @Schema(description = "화장실 정보", example = "1층 로비 옆")
        String restroomInfo,

        @Schema(description = "공간 분위기", example = "CALM")
        Mood mood,

        @Schema(description = "공간 크기", example = "LARGE")
        SpaceSize spaceSize,

        @Schema(description = "콘센트 환경", example = "MANY")
        OutletScore outletScore,

        @Schema(description = "혼잡도", example = "RELAX")
        CrowdStatus crowdStatus,

        @Schema(description = "태그 ID 리스트", example = "[1, 5, 12]")
        List<Long> tagIds,

        @Schema(description = "업로드된 사진 리스트")
        @Valid
        @Size(min = 1, message = "사진은 최소 1장 이상 등록해야 합니다")
        List<PlaceImageRequest> images
) {
}
