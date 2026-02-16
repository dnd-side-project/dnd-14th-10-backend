package io.dnd.goyo.domain.review.dto.request;

import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewUpdateRequest(
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5 이하여야 합니다")
        Integer rating,

        @NotNull(message = "태그는 필수입니다")
        @Size(min = 2, max = 5, message = "태그는 2~5개 선택해야 합니다")
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

        @Valid List<ReviewImageRequest> images,

        LocalDateTime visitedAt
) {
}
