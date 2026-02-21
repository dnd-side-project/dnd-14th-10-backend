package io.dnd.goyo.domain.review.dto.request;

import io.dnd.goyo.common.validation.HalfStep;
import io.dnd.goyo.common.validation.NoDuplicates;
import io.dnd.goyo.common.validation.SinglePrimary;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewUpdateRequest(
        @NotNull(message = "평점은 필수입니다")
        @DecimalMin(value = "0.5", message = "평점은 0.5 이상이어야 합니다")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다")
        @HalfStep
        BigDecimal rating,

        @NotNull(message = "태그는 필수입니다")
        @Size(min = 2, max = 5, message = "태그는 2~5개 선택해야 합니다")
        @NoDuplicates(message = "태그 ID는 중복될 수 없습니다")
        List<Long> tagIds,

        @NotNull(message = "분위기는 필수입니다")
        Mood mood,

        @NotNull(message = "공간 크기는 필수입니다")
        SpaceSize spaceSize,

        @NotNull(message = "콘센트 점수는 필수입니다")
        OutletScore outletScore,

        @NotNull(message = "혼잡도는 필수입니다")
        CrowdStatus crowdStatus,

        String content,

        @Valid @Size(max = 6, message = "리뷰 이미지는 최대 6개까지 등록할 수 있습니다") @SinglePrimary List<ReviewImageRequest> images,

        LocalDateTime visitedAt
) {
}
