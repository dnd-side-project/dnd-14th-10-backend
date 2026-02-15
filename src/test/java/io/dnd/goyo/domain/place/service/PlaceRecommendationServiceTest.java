package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagReader;
import io.dnd.goyo.domain.review.service.ReviewReader;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.AgeGroup;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlaceRecommendationServiceTest {

    @InjectMocks
    private PlaceRecommendationService placeRecommendationService;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private WishlistReader wishlistReader;

    @Mock
    private ReviewReader reviewReader;

    @Mock
    private PlaceTagReader placeTagReader;

    @Mock
    private UserReader userReader;

    @Mock
    private GeometryUtils geometryUtils;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void 신규_공간_조회_성공() {
        // given
        long userId = 1L;
        double longitude = 126.978;
        double latitude = 37.566;
        long regionCode = 11010L;
        PlaceCategory category = PlaceCategory.CAFE;

        List<Long> placeIds = List.of(10L, 20L);
        Place place1 = createPlace(10L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(20L, "카페B", 126.9836, 37.5700);

        given(placeRepository.findNewPlaceIdsByRegionCode(
                longitude, latitude, 3000.0, regionCode, "CAFE", 30, 6
        )).willReturn(placeIds);
        given(placeRepository.findAllByIdWithDetails(placeIds))
                .willReturn(List.of(place1, place2));
        given(wishlistReader.getWishedPlaceIds(userId, placeIds))
                .willReturn(List.of(10L));

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                userId, longitude, latitude, regionCode, category, null
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).isWished()).isTrue();
        assertThat(result.get(1).id()).isEqualTo(20L);
        assertThat(result.get(1).isWished()).isFalse();
    }

    @Test
    void 결과_없으면_빈_리스트_반환() {
        // given
        long regionCode = 11010L;
        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 3000.0, regionCode, "CAFE", 30, 6
        )).willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, regionCode, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).isEmpty();
        verify(placeRepository, never()).findAllByIdWithDetails(List.of());
    }

    @Test
    void 커스텀_반경_적용() {
        // given
        Integer customRadius = 5000;
        long regionCode = 11010L;

        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 5000.0, regionCode, "CAFE", 30, 6
        )).willReturn(List.of());

        // when
        placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, regionCode, PlaceCategory.CAFE, customRadius
        );

        // then
        verify(placeRepository).findNewPlaceIdsByRegionCode(
                126.978, 37.566, 5000.0, regionCode, "CAFE", 30, 6
        );
    }

    @Test
    void 인기_공간_조회_성공() {
        // given
        long userId = 1L;
        double longitude = 126.978;
        double latitude = 37.566;
        PlaceCategory category = PlaceCategory.CAFE;

        List<Long> placeIds = List.of(10L, 20L);
        Place place1 = createPlace(10L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(20L, "카페B", 126.9836, 37.5700);

        given(placeRepository.findPopularPlaceIds(
                longitude, latitude, 3000.0, "CAFE", 2, 3.5, 6
        )).willReturn(placeIds);
        given(placeRepository.findAllByIdWithDetails(placeIds))
                .willReturn(List.of(place1, place2));
        given(wishlistReader.getWishedPlaceIds(userId, placeIds))
                .willReturn(List.of(10L));

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                userId, longitude, latitude, category, null
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).isWished()).isTrue();
        assertThat(result.get(1).id()).isEqualTo(20L);
        assertThat(result.get(1).isWished()).isFalse();
    }

    @Test
    void 인기_공간_결과_없으면_빈_리스트_반환() {
        // given
        given(placeRepository.findPopularPlaceIds(
                126.978, 37.566, 3000.0, "CAFE", 2, 3.5, 6
        )).willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                1L, 126.978, 37.566, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).isEmpty();
        verify(placeRepository, never()).findAllByIdWithDetails(List.of());
    }

    @Test
    void 인기_공간_인기순_정렬_유지() {
        // given
        List<Long> orderedIds = List.of(30L, 10L, 20L);
        Place place1 = createPlace(10L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(20L, "카페B", 126.9836, 37.5700);
        Place place3 = createPlace(30L, "카페C", 126.9770, 37.5796);

        given(placeRepository.findPopularPlaceIds(
                126.978, 37.566, 3000.0, "CAFE", 2, 3.5, 6
        )).willReturn(orderedIds);
        given(placeRepository.findAllByIdWithDetails(orderedIds))
                .willReturn(List.of(place1, place2, place3));
        given(wishlistReader.getWishedPlaceIds(1L, orderedIds))
                .willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                1L, 126.978, 37.566, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).extracting(PlaceSummaryResponse::id)
                .containsExactly(30L, 10L, 20L);
    }

    @Test
    void 거리순_정렬_유지() {
        // given
        long regionCode = 11010L;
        List<Long> orderedIds = List.of(30L, 10L, 20L);
        Place place1 = createPlace(10L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(20L, "카페B", 126.9836, 37.5700);
        Place place3 = createPlace(30L, "카페C", 126.9770, 37.5796);

        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 3000.0, regionCode, "CAFE", 30, 6
        )).willReturn(orderedIds);
        given(placeRepository.findAllByIdWithDetails(orderedIds))
                .willReturn(List.of(place1, place2, place3));
        given(wishlistReader.getWishedPlaceIds(1L, orderedIds))
                .willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, regionCode, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).extracting(PlaceSummaryResponse::id)
                .containsExactly(30L, 10L, 20L);
    }

    @Test
    void 비슷한_성향_공간_조회_성공() {
        // given
        long userId = 1L;
        double longitude = 126.978;
        double latitude = 37.566;
        long regionCode = 11010L;
        PlaceCategory category = PlaceCategory.CAFE;

        User user = createUser(userId, Gender.FEMALE, 25);

        Map<Long, Double> userTagWeights = Map.of(1L, 1.0, 2L, 0.8);
        Map<Long, Double> groupTagNorm = Map.of(1L, 1.0, 3L, 0.5);
        List<Long> wishedPlaceIds = List.of(10L);
        List<Long> reviewedPlaceIds = List.of(20L);
        List<Long> candidateIds = List.of(30L, 40L);

        Place place1 = createPlace(30L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(40L, "카페B", 126.9900, 37.5800);

        Map<Long, List<Long>> placeTagMap = Map.of(
                30L, List.of(1L, 2L),
                40L, List.of(3L)
        );

        given(wishlistReader.getTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(userTagWeights);
        given(reviewReader.getReviewTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(Map.of());
        given(userReader.getUser(userId)).willReturn(user);
        given(wishlistReader.getGroupTagPopularity(eq(Gender.FEMALE), eq(AgeGroup.TWENTIES), any(LocalDateTime.class)))
                .willReturn(groupTagNorm);
        given(wishlistReader.getAllWishedPlaceIds(eq(userId), any(LocalDateTime.class)))
                .willReturn(wishedPlaceIds);
        given(reviewReader.getReviewedPlaceIds(eq(userId), any(LocalDateTime.class)))
                .willReturn(reviewedPlaceIds);

        given(placeTagReader.findCandidatePlaceIds(
                eq(List.of(1L, 2L, 3L)),
                eq(11010L),
                eq("CAFE"),
                argThat(list -> list.containsAll(List.of(10L, 20L)) && list.size() == 2)
        )).willReturn(candidateIds);

        given(placeRepository.findAllByIdWithDetails(candidateIds))
                .willReturn(List.of(place1, place2));
        given(placeTagReader.getPlaceTagMappings(candidateIds)).willReturn(placeTagMap);
        given(geometryUtils.calculateDistance(126.978, 37.566, 126.9769, 37.5759))
                .willReturn(500.0);
        given(geometryUtils.calculateDistance(126.978, 37.566, 126.9900, 37.5800))
                .willReturn(2500.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                userId, regionCode, category, longitude, latitude
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(30L);
        assertThat(result.get(1).id()).isEqualTo(40L);
    }

    @Test
    void 비슷한_성향_공간_후보_없으면_빈_리스트() {
        // given
        long userId = 1L;

        given(wishlistReader.getTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(Map.of());
        given(reviewReader.getReviewTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(Map.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                userId, 11010L, PlaceCategory.CAFE, 126.978, 37.566
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 비슷한_성향_공간_스코어_정렬() {
        // given
        long userId = 1L;
        User user = createUser(userId, Gender.MALE, 35);

        Map<Long, Double> userTagWeights = Map.of(1L, 1.0);
        List<Long> candidateIds = List.of(30L, 40L, 50L);

        Place place1 = createPlace(30L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(40L, "카페B", 126.9800, 37.5700);
        Place place3 = createPlace(50L, "카페C", 126.9900, 37.5850);

        Map<Long, List<Long>> placeTagMap = Map.of(
                30L, List.of(1L, 2L),
                40L, List.of(1L),
                50L, List.of(3L)
        );

        given(wishlistReader.getTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(userTagWeights);
        given(reviewReader.getReviewTagWeights(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(Map.of());
        given(userReader.getUser(userId)).willReturn(user);
        given(wishlistReader.getGroupTagPopularity(eq(Gender.MALE), eq(AgeGroup.THIRTIES), any(LocalDateTime.class)))
                .willReturn(Map.of());
        given(wishlistReader.getAllWishedPlaceIds(eq(userId), any(LocalDateTime.class)))
                .willReturn(List.of());
        given(reviewReader.getReviewedPlaceIds(eq(userId), any(LocalDateTime.class)))
                .willReturn(List.of());
        given(placeTagReader.findCandidatePlaceIds(List.of(1L), 11010L, "CAFE", List.of()))
                .willReturn(candidateIds);
        given(placeRepository.findAllByIdWithDetails(candidateIds))
                .willReturn(List.of(place1, place2, place3));
        given(placeTagReader.getPlaceTagMappings(candidateIds)).willReturn(placeTagMap);
        given(geometryUtils.calculateDistance(126.978, 37.566, 126.9769, 37.5759))
                .willReturn(500.0);
        given(geometryUtils.calculateDistance(126.978, 37.566, 126.9800, 37.5700))
                .willReturn(1500.0);
        given(geometryUtils.calculateDistance(126.978, 37.566, 126.9900, 37.5850))
                .willReturn(3000.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                userId, 11010L, PlaceCategory.CAFE, 126.978, 37.566
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo(30L);
        assertThat(result.get(1).id()).isEqualTo(40L);
        assertThat(result.get(2).id()).isEqualTo(50L);
    }

    private User createUser(Long id, Gender gender, int age) {
        User user = User.builder()
                .name("테스트유저")
                .nickname("테스트닉네임")
                .gender(gender)
                .age(age)
                .provider(Provider.KAKAO)
                .role(UserRole.USER)
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "ageGroup", AgeGroup.from(age));

        return user;
    }

    private Place createPlace(Long id, String name, double longitude, double latitude) {
        Place place = Place.builder()
                .name(name)
                .category(PlaceCategory.CAFE)
                .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
                .regionCode(1101005200L)
                .addressDetail("테스트 주소")
                .user(mock(User.class))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        ReflectionTestUtils.setField(place, "id", id);

        PlaceDetail placeDetail = PlaceDetail.of(place, 0, 0, 0, 0);
        ReflectionTestUtils.setField(place, "placeDetail", placeDetail);

        PlaceImage image = PlaceImage.of("place/test.jpg", true, 0);
        place.addImages(List.of(image));

        return place;
    }
}
