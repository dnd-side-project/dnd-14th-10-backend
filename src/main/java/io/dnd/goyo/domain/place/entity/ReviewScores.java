package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.review.entity.Review;

public record ReviewScores(
        double rating,
        int outletScore,
        int crowdScore,
        int spaceSizeScore,
        int quietScore
) {

    public static ReviewScores from(Review review) {
        return new ReviewScores(
                review.getRating() != null ? review.getRating() : 0.0,
                review.getOutletScore() != null ? review.getOutletScore().getScore() : 0,
                review.getCrowdStatus() != null ? review.getCrowdStatus().getScore() : 0,
                review.getSpaceSize() != null ? review.getSpaceSize().getScore() : 0,
                review.getMood() != null ? review.getMood().getScore() : 0
        );
    }

    public static ReviewScores from(
            Double rating,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize,
            Mood mood
    ) {
        return new ReviewScores(
                rating != null ? rating : 0.0,
                outletScore != null ? outletScore.getScore() : 0,
                crowdStatus != null ? crowdStatus.getScore() : 0,
                spaceSize != null ? spaceSize.getScore() : 0,
                mood != null ? mood.getScore() : 0
        );
    }
}
