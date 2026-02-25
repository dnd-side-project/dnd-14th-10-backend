package io.dnd.goyo.domain.badge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse;
import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse.CategoryProgress;
import io.dnd.goyo.domain.badge.entity.Badge;
import io.dnd.goyo.domain.badge.entity.UserBadge;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.repository.UserBadgeRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @InjectMocks
    private BadgeService badgeService;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Test
    @DisplayName("뱃지 진행상황 조회 성공")
    void 뱃지_진행상황_조회_성공() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserStats userStats = UserStats.of(user);
        userStats.incrementReviewCount();

        Badge reviewBadge = Badge.builder().code("REVIEW_1").name("첫 리뷰").build();
        UserBadge userBadge = UserBadge.of(user, reviewBadge);

        given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
        given(userBadgeRepository.findAllByUserId(userId)).willReturn(List.of(userBadge));

        // when
        BadgeProgressResponse response = badgeService.getBadgeProgress(userId);

        // then
        assertThat(response.categories()).hasSize(3);

        CategoryProgress reviewCategory = response.categories().stream()
                .filter(c -> c.activityType() == ActivityType.REVIEW)
                .findFirst().orElseThrow();
        assertThat(reviewCategory.currentCount()).isEqualTo(1);
        assertThat(reviewCategory.badges()).hasSize(3);
        assertThat(reviewCategory.badges().get(0).achieved()).isTrue();
        assertThat(reviewCategory.badges().get(1).achieved()).isFalse();
    }

    @Test
    @DisplayName("뱃지가 없는 사용자 조회")
    void 뱃지가_없는_사용자_조회() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserStats userStats = UserStats.of(user);

        given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
        given(userBadgeRepository.findAllByUserId(userId)).willReturn(List.of());

        // when
        BadgeProgressResponse response = badgeService.getBadgeProgress(userId);

        // then
        assertThat(response.categories()).hasSize(3);
        response.categories().forEach(category -> {
            assertThat(category.currentCount()).isEqualTo(0);
            category.badges().forEach(badge ->
                    assertThat(badge.achieved()).isFalse()
            );
        });
    }

    @Test
    @DisplayName("UserBadge 없어도 카운트가 임계값 이상이면 achieved true")
    void UserBadge_없어도_카운트가_임계값_이상이면_achieved_true() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        UserStats userStats = UserStats.of(user);
        userStats.incrementPlaceCount(); // placeCount = 1, PLACE_1 threshold = 1

        given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
        given(userBadgeRepository.findAllByUserId(userId)).willReturn(List.of());

        // when
        BadgeProgressResponse response = badgeService.getBadgeProgress(userId);

        // then
        CategoryProgress placeCategory = response.categories().stream()
                .filter(c -> c.activityType() == ActivityType.PLACE)
                .findFirst().orElseThrow();

        assertThat(placeCategory.currentCount()).isEqualTo(1);
        // PLACE_1: threshold 1, currentCount 1 → UserBadge 없어도 achieved = true
        assertThat(placeCategory.badges().get(0).achieved()).isTrue();
        assertThat(placeCategory.badges().get(0).achievedAt()).isNull();
        // PLACE_7: threshold 7, currentCount 1 → achieved = false
        assertThat(placeCategory.badges().get(1).achieved()).isFalse();
    }

    @Test
    @DisplayName("UserStats가 없으면 예외 발생")
    void UserStats가_없으면_예외_발생() {
        // given
        Long userId = 1L;
        given(userStatsRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> badgeService.getBadgeProgress(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
