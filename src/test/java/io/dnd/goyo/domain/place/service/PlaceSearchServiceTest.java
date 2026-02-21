package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.dto.PlaceWithDistance;
import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceFilterResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.entity.RegionCode;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @InjectMocks
    private PlaceSearchService placeSearchService;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private FileStorage fileStorage;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    @DisplayName("공간 필터 검색")
    class GetFilteredPlaces {

        @Test
        void 필터_검색_성공() {
            // given
            PlaceFilterRequest request = new PlaceFilterRequest(
                    PlaceCategory.CAFE, null, null, null, null, 10
            );
            double longitude = 127.0;
            double latitude = 37.5;

            List<PlaceWithDistance> placesWithDistance = List.of(
                    new PlaceWithDistance(1L, 100.5),
                    new PlaceWithDistance(2L, 200.3)
            );

            List<Place> places = List.of(
                    createMockPlace(1L),
                    createMockPlace(2L)
            );

            given(placeRepository.findByFilterWithDistance(any(), eq(longitude), eq(latitude), eq(10)))
                    .willReturn(placesWithDistance);
            given(placeRepository.findAllByIdWithDetails(anyList()))
                    .willReturn(places);
            given(fileStorage.generatePublicUrl("place/image.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(request, longitude, latitude);

            // then
            assertThat(response.places()).hasSize(2);
            assertThat(response.lastDistance()).isEqualTo(200.3);
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        void 결과_없으면_빈_응답() {
            // given
            PlaceFilterRequest request = new PlaceFilterRequest(
                    PlaceCategory.CAFE, null, null, null, null, 10
            );

            given(placeRepository.findByFilterWithDistance(any(), eq(127.0), eq(37.5), eq(10)))
                    .willReturn(List.of());

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(request, 127.0, 37.5);

            // then
            assertThat(response.places()).isEmpty();
            assertThat(response.lastDistance()).isNull();
            assertThat(response.hasNext()).isFalse();
        }

        @Test
        void hasNext_확인() {
            // given
            PlaceFilterRequest request = new PlaceFilterRequest(
                    PlaceCategory.CAFE, null, null, null, null, 10
            );

            List<PlaceWithDistance> placesWithDistance = IntStream.rangeClosed(1, 11)
                    .mapToObj(i -> new PlaceWithDistance((long) i, i * 100.0))
                    .toList();

            List<Place> places = IntStream.rangeClosed(1, 10)
                    .mapToObj(i -> createMockPlace((long) i))
                    .toList();

            given(placeRepository.findByFilterWithDistance(any(), eq(127.0), eq(37.5), eq(10)))
                    .willReturn(placesWithDistance);
            given(placeRepository.findAllByIdWithDetails(anyList()))
                    .willReturn(places);
            given(fileStorage.generatePublicUrl("place/image.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(request, 127.0, 37.5);

            // then
            assertThat(response.places()).hasSize(10);
            assertThat(response.lastDistance()).isEqualTo(1000.0);
            assertThat(response.hasNext()).isTrue();
        }

        @Test
        void size_기본값_적용() {
            // given
            PlaceFilterRequest request = new PlaceFilterRequest(
                    PlaceCategory.CAFE, null, null, null, null, null
            );

            given(placeRepository.findByFilterWithDistance(any(), eq(127.0), eq(37.5), eq(10)))
                    .willReturn(List.of());

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(request, 127.0, 37.5);

            // then
            assertThat(response.places()).isEmpty();
        }

    }

    private Place createMockPlace(Long id) {
        PlaceDetail placeDetail = mock(PlaceDetail.class);
        given(placeDetail.getMood()).willReturn(Mood.CALM);
        given(placeDetail.getSpaceSize()).willReturn(SpaceSize.MEDIUM);
        given(placeDetail.getWishCount()).willReturn(5);

        PlaceImage image = mock(PlaceImage.class);
        given(image.getImageKey()).willReturn("place/image.jpg");
        given(image.getSequence()).willReturn(0);
        given(image.isRepresentativeFlag()).willReturn(true);

        Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));

        Place place = mock(Place.class);
        given(place.getId()).willReturn(id);
        given(place.getName()).willReturn("테스트 카페 " + id);
        given(place.getCategory()).willReturn(PlaceCategory.CAFE);
        given(place.getAddressDetail()).willReturn("서울시 강남구");
        given(place.getRegionCode()).willReturn(new RegionCode(1168010100L));
        given(place.getLocation()).willReturn(location);
        given(place.getImages()).willReturn(new ArrayList<>(List.of(image)));
        given(place.getPlaceDetail()).willReturn(placeDetail);

        return place;
    }
}