package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import java.time.LocalTime;
import java.util.List;
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

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Test
    void 신규_공간_조회_성공() {
        // given
        long userId = 1L;
        double longitude = 126.978;
        double latitude = 37.566;
        int regionCode = 11010;
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
        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 3000.0, 11010, "CAFE", 30, 6
        )).willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).isEmpty();
        verify(placeRepository, never()).findAllByIdWithDetails(List.of());
    }

    @Test
    void 커스텀_반경_적용() {
        // given
        Integer customRadius = 5000;

        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 5000.0, 11010, "CAFE", 30, 6
        )).willReturn(List.of());

        // when
        placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, 11010, PlaceCategory.CAFE, customRadius
        );

        // then
        verify(placeRepository).findNewPlaceIdsByRegionCode(
                126.978, 37.566, 5000.0, 11010, "CAFE", 30, 6
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
        List<Long> orderedIds = List.of(30L, 10L, 20L);
        Place place1 = createPlace(10L, "카페A", 126.9769, 37.5759);
        Place place2 = createPlace(20L, "카페B", 126.9836, 37.5700);
        Place place3 = createPlace(30L, "카페C", 126.9770, 37.5796);

        given(placeRepository.findNewPlaceIdsByRegionCode(
                126.978, 37.566, 3000.0, 11010, "CAFE", 30, 6
        )).willReturn(orderedIds);
        given(placeRepository.findAllByIdWithDetails(orderedIds))
                .willReturn(List.of(place1, place2, place3));
        given(wishlistReader.getWishedPlaceIds(1L, orderedIds))
                .willReturn(List.of());

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                1L, 126.978, 37.566, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).extracting(PlaceSummaryResponse::id)
                .containsExactly(30L, 10L, 20L);
    }

    private Place createPlace(Long id, String name, double longitude, double latitude) {
        Place place = Place.builder()
                .name(name)
                .category(PlaceCategory.CAFE)
                .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
                .regionCode(110100520)
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
