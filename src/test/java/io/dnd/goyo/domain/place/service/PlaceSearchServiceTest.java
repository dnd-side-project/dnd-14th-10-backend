package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.storage.FileStorage;
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
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private WishlistReader wishlistReader;

    @Mock
    private FileStorage fileStorage;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    @DisplayName("공간 필터 검색")
    class GetFilteredPlaces {

        @Test
        void 사이즈가_null이면_기본값_10_사용() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, null);

            given(placeRepository.findByFilter(request, 10)).willReturn(List.of());

            // when
            placeSearchService.getFilteredPlaces(userId, request);

            // then
            verify(placeRepository).findByFilter(request, 10);
        }

        @Test
        void 사이즈가_지정되면_해당_값_사용() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, 5);

            given(placeRepository.findByFilter(request, 5)).willReturn(List.of());

            // when
            placeSearchService.getFilteredPlaces(userId, request);

            // then
            verify(placeRepository).findByFilter(request, 5);
        }

        @Test
        void 결과가_사이즈_초과면_다음_페이지_있음() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, 2);

            List<Long> placeIds = List.of(1L, 2L, 3L);
            Place place1 = createMockPlace(1L);
            Place place2 = createMockPlace(2L);

            given(placeRepository.findByFilter(request, 2)).willReturn(placeIds);
            given(placeRepository.findAllByIdWithDetails(List.of(1L, 2L)))
                    .willReturn(List.of(place1, place2));
            given(wishlistReader.getWishedPlaceIdSet(userId, List.of(1L, 2L))).willReturn(Set.of());
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(userId, request);

            // then
            assertThat(response.hasNext()).isTrue();
            assertThat(response.places()).hasSize(2);
            assertThat(response.lastPlaceId()).isEqualTo(2L);
        }

        @Test
        void 결과가_사이즈_이하면_다음_페이지_없음() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, 5);

            List<Long> placeIds = List.of(1L, 2L);
            Place place1 = createMockPlace(1L);
            Place place2 = createMockPlace(2L);

            given(placeRepository.findByFilter(request, 5)).willReturn(placeIds);
            given(placeRepository.findAllByIdWithDetails(placeIds))
                    .willReturn(List.of(place1, place2));
            given(wishlistReader.getWishedPlaceIdSet(userId, placeIds)).willReturn(Set.of());
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(userId, request);

            // then
            assertThat(response.hasNext()).isFalse();
            assertThat(response.places()).hasSize(2);
        }

        @Test
        void 위시한_공간은_true() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, null);

            List<Long> placeIds = List.of(1L, 2L);
            Place place1 = createMockPlace(1L);
            Place place2 = createMockPlace(2L);

            given(placeRepository.findByFilter(request, 10)).willReturn(placeIds);
            given(placeRepository.findAllByIdWithDetails(placeIds))
                    .willReturn(List.of(place1, place2));
            given(wishlistReader.getWishedPlaceIdSet(userId, placeIds)).willReturn(Set.of(1L));
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(userId, request);

            // then
            assertThat(response.places().get(0).isWished()).isTrue();
            assertThat(response.places().get(1).isWished()).isFalse();
        }

        @Test
        void 비로그인이면_모든_공간이_위시_false() {
            // given
            Long userId = null;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, null);

            List<Long> placeIds = List.of(1L, 2L);
            Place place1 = createMockPlace(1L);
            Place place2 = createMockPlace(2L);

            given(placeRepository.findByFilter(request, 10)).willReturn(placeIds);
            given(placeRepository.findAllByIdWithDetails(placeIds))
                    .willReturn(List.of(place1, place2));
            given(wishlistReader.getWishedPlaceIdSet(userId, placeIds)).willReturn(Set.of());
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(userId, request);

            // then
            assertThat(response.places()).allMatch(p -> !p.isWished());
        }

        @Test
        void 마지막_공간_ID_반환() {
            // given
            Long userId = 1L;
            PlaceFilterRequest request = new PlaceFilterRequest(null, null, null, null, null, null);

            List<Long> placeIds = List.of(10L, 20L, 30L);
            List<Place> places = placeIds.stream().map(this::createMockPlace).toList();

            given(placeRepository.findByFilter(request, 10)).willReturn(placeIds);
            given(placeRepository.findAllByIdWithDetails(placeIds)).willReturn(places);
            given(wishlistReader.getWishedPlaceIdSet(userId, placeIds)).willReturn(Set.of());
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            PlaceFilterResponse response = placeSearchService.getFilteredPlaces(userId, request);

            // then
            assertThat(response.lastPlaceId()).isEqualTo(30L);
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
}
