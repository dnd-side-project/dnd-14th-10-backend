package io.dnd.goyo.domain.badge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.domain.badge.entity.Badge;
import io.dnd.goyo.domain.badge.entity.UserBadge;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.badge.repository.BadgeRepository;
import io.dnd.goyo.domain.badge.repository.UserBadgeRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import io.dnd.goyo.domain.user.service.UserReader;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BadgeEventListenerTest {

    @InjectMocks
    private BadgeEventListener badgeEventListener;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private UserReader userReader;

    @Nested
    @DisplayName("리뷰 활동 이벤트 처리 시")
    class HandleReviewActivity {

        @Test
        void 리뷰_생성_시_카운트_증가_및_뱃지_평가() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            UserStats userStats = UserStats.of(user);

            Badge badge = Badge.builder().code("REVIEW_1").name("첫 리뷰").build();

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
            given(userReader.getUser(userId)).willReturn(user);
            given(badgeRepository.findByCode("REVIEW_1")).willReturn(Optional.of(badge));
            given(userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())).willReturn(false);

            ActivityEvent event = new ActivityEvent(userId, ActivityType.REVIEW, 1, 0);

            // when
            badgeEventListener.handleActivity(event);

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(1);
            verify(userBadgeRepository).save(any(UserBadge.class));
        }

        @Test
        void 이미_획득한_뱃지는_중복_지급하지_않음() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            UserStats userStats = UserStats.of(user);

            Badge badge = Badge.builder().code("REVIEW_1").name("첫 리뷰").build();

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
            given(userReader.getUser(userId)).willReturn(user);
            given(badgeRepository.findByCode("REVIEW_1")).willReturn(Optional.of(badge));
            given(userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())).willReturn(true);

            ActivityEvent event = new ActivityEvent(userId, ActivityType.REVIEW, 1, 0);

            // when
            badgeEventListener.handleActivity(event);

            // then
            verify(userBadgeRepository, never()).save(any(UserBadge.class));
        }

        @Test
        void 리뷰_삭제_시_카운트_감소_및_뱃지_평가_안함() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);
            userStats.incrementReviewCount();

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));

            ActivityEvent event = new ActivityEvent(userId, ActivityType.REVIEW, -1, 0);

            // when
            badgeEventListener.handleActivity(event);

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(0);
            verify(userBadgeRepository, never()).save(any(UserBadge.class));
        }
    }

    @Nested
    @DisplayName("이미지 활동 이벤트 처리 시")
    class HandleImageActivity {

        @Test
        void 리뷰_생성_시_이미지_포함이면_이미지_카운트도_증가() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
            given(userReader.getUser(userId)).willReturn(user);
            given(badgeRepository.findByCode("REVIEW_1")).willReturn(Optional.empty());

            ActivityEvent event = new ActivityEvent(userId, ActivityType.REVIEW, 1, 1);

            // when
            badgeEventListener.handleActivity(event);

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(1);
            assertThat(userStats.getImageCount()).isEqualTo(1);
        }

        @Test
        void 이미지_감소_시_이미지_카운트_감소() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);
            userStats.incrementImageCount();

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));

            ActivityEvent event = new ActivityEvent(userId, ActivityType.IMAGE, 0, -1);

            // when
            badgeEventListener.handleActivity(event);

            // then
            assertThat(userStats.getImageCount()).isEqualTo(0);
            verify(userBadgeRepository, never()).save(any(UserBadge.class));
        }
    }

    @Nested
    @DisplayName("장소 활동 이벤트 처리 시")
    class HandlePlaceActivity {

        @Test
        void 장소_등록_시_카운트_증가_및_이미지_카운트_증가() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            given(user.getId()).willReturn(userId);
            UserStats userStats = UserStats.of(user);

            Badge badge = Badge.builder().code("PLACE_1").name("첫 장소").build();

            given(userStatsRepository.findByUserId(userId)).willReturn(Optional.of(userStats));
            given(userReader.getUser(userId)).willReturn(user);
            given(badgeRepository.findByCode("PLACE_1")).willReturn(Optional.of(badge));
            given(userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())).willReturn(false);

            ActivityEvent event = new ActivityEvent(userId, ActivityType.PLACE, 1, 1);

            // when
            badgeEventListener.handleActivity(event);

            // then
            assertThat(userStats.getPlaceCount()).isEqualTo(1);
            assertThat(userStats.getImageCount()).isEqualTo(1);
            verify(userBadgeRepository).save(any(UserBadge.class));
        }
    }

    @Test
    @DisplayName("UserStats가 없으면 무시")
    void UserStats가_없으면_무시() {
        // given
        Long userId = 1L;
        given(userStatsRepository.findByUserId(userId)).willReturn(Optional.empty());

        ActivityEvent event = new ActivityEvent(userId, ActivityType.REVIEW, 1, 0);

        // when
        badgeEventListener.handleActivity(event);

        // then
        verify(userBadgeRepository, never()).save(any(UserBadge.class));
    }
}
