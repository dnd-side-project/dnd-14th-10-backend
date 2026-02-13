package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.common.util.GeometryUtils;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class PlaceRecommendationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4-alpine")
                    .asCompatibleSubstituteFor("postgres")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PlaceRecommendationService placeRecommendationService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceDetailRepository placeDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private GeometryUtils geometryUtils;

    @MockitoBean
    private FileStorage fileStorage;

    @Autowired
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("테스트유저")
                .nickname("테스트닉네임")
                .gender(Gender.MALE)
                .provider(Provider.KAKAO)
                .providerId("test_001")
                .role(UserRole.USER)
                .build());
    }

    @Test
    void 반경_내_같은_지역코드_같은_카테고리만_조회() {
        // given
        savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520);
        savePlace("종각 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530);
        savePlace("경복궁 도서관", PlaceCategory.PUBLIC, 126.9770, 37.5796, 110100220);
        savePlace("강남 카페", PlaceCategory.CAFE, 127.0276, 37.4979, 116800110);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), 126.978, 37.570, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PlaceSummaryResponse::name)
                .containsExactlyInAnyOrder("광화문 카페", "종각 카페");
    }

    @Test
    void 거리순_정렬_Haversine_공식과_일치() {
        // given
        double baseLon = 126.978, baseLat = 37.570;
        savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520);
        savePlace("종각 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530);

        double distToGwanghwamun = geometryUtils.calculateDistance(baseLat, baseLon, 37.5759, 126.9769);
        double distToJonggak = geometryUtils.calculateDistance(baseLat, baseLon, 37.5700, 126.9836);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), baseLon, baseLat, 11010, PlaceCategory.CAFE, null
        );

        // then - DB(ST_Distance)와 Haversine 모두 종각이 더 가까움
        assertThat(distToJonggak).isLessThan(distToGwanghwamun);
        assertThat(result.get(0).name()).isEqualTo("종각 카페");
        assertThat(result.get(1).name()).isEqualTo("광화문 카페");
    }

    @Test
    void 찜한_장소_표시() {
        // given
        Place place = savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520);
        wishlistRepository.saveAndFlush(Wishlist.of(user, place));

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), 126.978, 37.570, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().isWished()).isTrue();
    }

    @Test
    void 커스텀_반경_적용() {
        // given
        double baseLon = 126.978, baseLat = 37.570;
        int radiusMeters = 1000;
        savePlace("가까운 카페", PlaceCategory.CAFE, 126.978, 37.574, 110100520);
        savePlace("먼 카페", PlaceCategory.CAFE, 126.960, 37.570, 110100520);

        double distToNear = geometryUtils.calculateDistance(baseLat, baseLon, 37.574, 126.978);
        double distToFar = geometryUtils.calculateDistance(baseLat, baseLon, 37.570, 126.960);

        // when - 반경 1000m
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), baseLon, baseLat, 11010, PlaceCategory.CAFE, radiusMeters
        );

        // then - Haversine으로도 가까운 카페만 반경 내
        assertThat(distToNear).isLessThan(radiusMeters);
        assertThat(distToFar).isGreaterThan(radiusMeters);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("가까운 카페");
    }

    @Test
    void 결과_없으면_빈_리스트_반환() {
        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), 126.978, 37.570, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 대표_이미지_URL_반환() {
        // given
        savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                user.getId(), 126.978, 37.570, 11010, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result.getFirst().representativeImageUrl()).isEqualTo("places/test.jpg");
    }

    @Test
    void 인기_공간_인기순_정렬() {
        // given
        savePlaceWithDetails("인기 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520, 50, 30, 120.0);
        savePlaceWithDetails("보통 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530, 10, 5, 20.0);
        savePlaceWithDetails("핫플 카페", PlaceCategory.CAFE, 126.9770, 37.5796, 110100520, 100, 80, 320.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(PlaceSummaryResponse::name)
                .containsExactly("핫플 카페", "인기 카페", "보통 카페");
    }

    @Test
    void 인기_공간_같은_카테고리만_조회() {
        // given
        savePlaceWithDetails("종로 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520, 50, 30, 120.0);
        savePlaceWithDetails("종로 도서관", PlaceCategory.PUBLIC, 126.9770, 37.5796, 110100220, 100, 80, 320.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("종로 카페");
    }

    @Test
    void 인기_공간_찜한_장소와_안_찜한_장소_구분() {
        // given
        Place wishedPlace = savePlaceWithDetails("찜한 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520, 50, 30, 120.0);
        savePlaceWithDetails("안찜한 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530, 30, 10, 40.0);
        wishlistRepository.saveAndFlush(Wishlist.of(user, wishedPlace));

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(2);
        PlaceSummaryResponse wished = result.stream()
                .filter(r -> r.name().equals("찜한 카페")).findFirst().get();
        PlaceSummaryResponse notWished = result.stream()
                .filter(r -> r.name().equals("안찜한 카페")).findFirst().get();
        assertThat(wished.isWished()).isTrue();
        assertThat(notWished.isWished()).isFalse();
    }

    @Test
    void 인기_공간_평균_별점_3점_미만_제외() {
        // given
        savePlaceWithDetails("좋은 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520, 50, 10, 40.0);
        savePlaceWithDetails("나쁜 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530, 100, 10, 20.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("좋은 카페");
    }

    @Test
    void 인기_공간_리뷰_없으면_필터_통과_별점_낮으면_제외() {
        // given
        savePlaceWithDetails("리뷰없는 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 110100520, 30, 0, 0.0);
        savePlaceWithDetails("별점높은 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 110100530, 20, 10, 45.0);
        savePlaceWithDetails("별점낮은 카페", PlaceCategory.CAFE, 126.9770, 37.5796, 110100520, 40, 10, 20.0);

        // when
        List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
        );

        // then - 리뷰 0개는 통과, 평균 4.5 통과, 평균 2.0 제외
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PlaceSummaryResponse::name)
                .containsExactlyInAnyOrder("리뷰없는 카페", "별점높은 카페");
    }

    private Place savePlaceWithDetails(
            String name, PlaceCategory category,
            double longitude, double latitude, int regionCode,
            int wishCount, int reviewCount, double totalRating
    ) {
        Place place = Place.builder()
                .name(name)
                .category(category)
                .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
                .regionCode(regionCode)
                .addressDetail("테스트 주소")
                .user(user)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        place.addImages(List.of(PlaceImage.of("places/test.jpg", true, 0)));
        placeRepository.saveAndFlush(place);
        PlaceDetail detail = PlaceDetail.of(place, 50, 50, 50, 50);
        ReflectionTestUtils.setField(detail, "wishCount", wishCount);
        ReflectionTestUtils.setField(detail, "reviewCount", reviewCount);
        ReflectionTestUtils.setField(detail, "totalRating", totalRating);
        placeDetailRepository.saveAndFlush(detail);
        entityManager.clear();

        return place;
    }

    private Place savePlace(String name, PlaceCategory category,
            double longitude, double latitude, int regionCode) {
        Place place = Place.builder()
                .name(name)
                .category(category)
                .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
                .regionCode(regionCode)
                .addressDetail("테스트 주소")
                .user(user)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        place.addImages(List.of(PlaceImage.of("places/test.jpg", true, 0)));
        placeRepository.saveAndFlush(place);
        placeDetailRepository.saveAndFlush(PlaceDetail.of(place, 100, 0, 50, 75));
        entityManager.clear();

        return place;
    }
}
