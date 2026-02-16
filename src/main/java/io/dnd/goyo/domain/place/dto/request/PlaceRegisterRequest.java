package io.dnd.goyo.domain.place.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.locationtech.jts.geom.Point;

@Schema(description = "장소 등록 요청")
public record PlaceRegisterRequest(
        @Schema(description = "장소 이름", example = "고작 아지트")
        @NotBlank(message = "장소 이름은 필수입니다")
        String name,

        @Schema(description = "장소 카테고리", example = "CAFE")
        @NotNull(message = "카테고리는 필수입니다")
        PlaceCategory category,

        @Schema(description = "위도", example = "37.5665")
        @NotNull(message = "위도는 필수입니다")
        Double latitude,

        @Schema(description = "경도", example = "126.9780")
        @NotNull(message = "경도는 필수입니다")
        Double longitude,

        @Schema(description = "층수", example = "2")
        Integer floorInfo,

        @Schema(description = "영업 시작 시간 (HH:mm)", example = "10:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @Schema(description = "영업 종료 시간 (HH:mm)", example = "02:00", type = "string")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime,

        @Schema(description = "행정구역 코드 (10자리)", example = "1168010100")
        @NotNull(message = "행정구역 코드는 필수입니다")
        Long regionCode,

        @Schema(description = "상세 주소", example = "청계천로 101")
        @NotBlank(message = "상세 주소는 필수입니다")
        String addressDetail,

        @Schema(description = "화장실 정보", example = "1층 로비 옆")
        String restroomInfo,

        @Schema(description = "콘센트 만족도", example = "AVERAGE")
        @NotNull(message = "콘센트 만족도는 필수입니다")
        OutletScore outletScore,

        @Schema(description = "장소 크기", example = "MEDIUM")
        @NotNull(message = "장소 크기는 필수입니다")
        SpaceSize spaceSize,

        @Schema(description = "혼잡도", example = "RELAX")
        @NotNull(message = "혼잡도는 필수입니다")
        CrowdStatus crowdStatus,

        @Schema(description = "분위기", example = "CALM")
        @NotNull(message = "분위기는 필수입니다")
        Mood mood,

        @Schema(description = "태그 ID 리스트", example = "[1, 5, 12]")
        List<Long> tagIds,

        @Schema(description = "업로드된 사진 리스트")
        @NotEmpty(message = "사진은 최소 1장 이상 등록해야 합니다")
        @Valid
        List<PlaceImageRequest> images
) {
    public Place toPlaceEntity(User user, Point location) {
        return Place.builder()
                .name(name)
                .category(category)
                .location(location)
                .regionCode(regionCode)
                .addressDetail(addressDetail)
                .user(user)
                .floorInfo(floorInfo)
                .openTime(openTime)
                .closeTime(closeTime)
                .restroomInfo(restroomInfo)
                .build();
    }

    public List<PlaceImage> toImageEntities() {
        return images.stream()
                .filter(Objects::nonNull)
                .map(PlaceImageRequest::toEntity)
                .toList();
    }

    public PlaceDetail toPlaceDetailEntity(Place place) {
        return PlaceDetail.of(
                place,
                outletScore.getScore(),
                crowdStatus.getScore(),
                spaceSize.getScore(),
                mood.getScore()
        );
    }
}
