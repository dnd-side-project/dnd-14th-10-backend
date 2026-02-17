package io.dnd.goyo.domain.badge.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BadgeCodeTest {

    @Test
    @DisplayName("REVIEW 타입 뱃지 코드 조회")
    void REVIEW_타입_뱃지_코드_조회() {
        // when
        List<BadgeCode> reviewBadges = BadgeCode.getByActivityType(ActivityType.REVIEW);

        // then
        assertThat(reviewBadges).hasSize(3);
        assertThat(reviewBadges).extracting(BadgeCode::getCode)
                .containsExactly("REVIEW_1", "REVIEW_25", "REVIEW_80");
    }

    @Test
    @DisplayName("PLACE 타입 뱃지 코드 조회")
    void PLACE_타입_뱃지_코드_조회() {
        // when
        List<BadgeCode> placeBadges = BadgeCode.getByActivityType(ActivityType.PLACE);

        // then
        assertThat(placeBadges).hasSize(3);
        assertThat(placeBadges).extracting(BadgeCode::getThreshold)
                .containsExactly(1, 7, 20);
    }

    @Test
    @DisplayName("IMAGE 타입 뱃지 코드 조회")
    void IMAGE_타입_뱃지_코드_조회() {
        // when
        List<BadgeCode> imageBadges = BadgeCode.getByActivityType(ActivityType.IMAGE);

        // then
        assertThat(imageBadges).hasSize(3);
        assertThat(imageBadges).extracting(BadgeCode::getThreshold)
                .containsExactly(5, 30, 80);
    }
}
