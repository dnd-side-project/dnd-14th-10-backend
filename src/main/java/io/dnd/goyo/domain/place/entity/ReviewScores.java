package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.review.entity.Review;

public record ReviewScores(
        int rating,
        int outletScore,
        int crowdScore,
        int spaceSizeScore,
        int quietScore
) {

    public static ReviewScores from(Review review) {
        return new ReviewScores(
                review.getRating(),
                review.getOutletScore().getScore(),
                review.getCrowdStatus().getScore(),
                review.getSpaceSize().getScore(),
                review.getMood().getScore()
        );
    }

    public static ReviewScores from(
            int rating,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize,
            Mood mood
    ) {
        return new ReviewScores(
                rating,
                outletScore.getScore(),
                crowdStatus.getScore(),
                spaceSize.getScore(),
                mood.getScore()
        );
    }
}
