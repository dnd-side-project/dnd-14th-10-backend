package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.entity.PlaceTag;
import io.dnd.goyo.domain.placetag.repository.PlaceTagRepository;
import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.entity.ReviewTag;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.review.repository.ReviewRepository;
import io.dnd.goyo.domain.review.repository.ReviewTagRepository;
import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.enums.TagType;
import io.dnd.goyo.domain.tag.repository.TagRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private TagRepository tagRepository;

    @Autowired
    private PlaceTagRepository placeTagRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewTagRepository reviewTagRepository;

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
                .age(30)
                .provider(Provider.KAKAO)
                .providerId("test_001")
                .role(UserRole.USER)
                .build());
    }

    @Nested
    @DisplayName("신규 공간 조회 (/new)")
    class NewPlacesTest {

        @Test
        void 반경_내_같은_지역코드_같은_카테고리만_조회() {
            // given
            savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L);
            savePlace("종각 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L);
            savePlace("경복궁 도서관", PlaceCategory.PUBLIC, 126.9770, 37.5796, 1101002200L);
            savePlace("강남 카페", PlaceCategory.CAFE, 127.0276, 37.4979, 1168001100L);

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                    user.getId(), 126.978, 37.570, 11010L, PlaceCategory.CAFE, null
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
            savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L);
            savePlace("종각 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L);

            double distToGwanghwamun = geometryUtils.calculateDistance(baseLon, baseLat, 126.9769, 37.5759);
            double distToJonggak = geometryUtils.calculateDistance(baseLon, baseLat, 126.9836, 37.5700);

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                    user.getId(), baseLon, baseLat, 11010L, PlaceCategory.CAFE, null
            );

            // then - DB(ST_Distance)와 Haversine 모두 종각이 더 가까움
            assertThat(distToJonggak).isLessThan(distToGwanghwamun);
            assertThat(result.get(0).name()).isEqualTo("종각 카페");
            assertThat(result.get(1).name()).isEqualTo("광화문 카페");
        }

        @Test
        void 찜한_장소_표시() {
            // given
            Place place = savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L);
            saveWishlist(user, place);

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                    user.getId(), 126.978, 37.570, 11010L, PlaceCategory.CAFE, null
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
            savePlace("가까운 카페", PlaceCategory.CAFE, 126.978, 37.574, 1101005200L);
            savePlace("먼 카페", PlaceCategory.CAFE, 126.960, 37.570, 1101005200L);

            double distToNear = geometryUtils.calculateDistance(baseLon, baseLat, 126.978, 37.574);
            double distToFar = geometryUtils.calculateDistance(baseLon, baseLat, 126.960, 37.570);

            // when - 반경 1000m
            List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                    user.getId(), baseLon, baseLat, 11010L, PlaceCategory.CAFE, radiusMeters
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
                    user.getId(), 126.978, 37.570, 11010L, PlaceCategory.CAFE, null
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 대표_이미지_URL_반환() {
            // given
            savePlace("광화문 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L);

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getNewPlaces(
                    user.getId(), 126.978, 37.570, 11010L, PlaceCategory.CAFE, null
            );

            // then
            assertThat(result.getFirst().representativeImageUrl()).isEqualTo("places/test.jpg");
        }
    }

    @Nested
    @DisplayName("인기 공간 조회 (/popular)")
    class PopularPlacesTest {

        @Test
        void 인기_공간_인기순_정렬() {
            // given
            savePlaceWithDetails("인기 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L, 50, 30, 120.0);
            savePlaceWithDetails("보통 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L, 10, 5, 20.0);
            savePlaceWithDetails("핫플 카페", PlaceCategory.CAFE, 126.9770, 37.5796, 1101005200L, 100, 80, 320.0);

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
            savePlaceWithDetails("종로 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L, 50, 30, 120.0);
            savePlaceWithDetails("종로 도서관", PlaceCategory.PUBLIC, 126.9770, 37.5796, 1101002200L, 100, 80, 320.0);

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
            Place wishedPlace = savePlaceWithDetails("찜한 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L, 50, 30, 120.0);
            savePlaceWithDetails("안찜한 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L, 30, 10, 40.0);
            saveWishlist(user, wishedPlace);

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
            savePlaceWithDetails("좋은 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L, 50, 10, 40.0);
            savePlaceWithDetails("나쁜 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L, 100, 10, 20.0);

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
            savePlaceWithDetails("리뷰없는 카페", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L, 30, 0, 0.0);
            savePlaceWithDetails("별점높은 카페", PlaceCategory.CAFE, 126.9836, 37.5700, 1101005300L, 20, 10, 45.0);
            savePlaceWithDetails("별점낮은 카페", PlaceCategory.CAFE, 126.9770, 37.5796, 1101005200L, 40, 10, 20.0);

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getPopularPlaces(
                    user.getId(), 126.978, 37.570, PlaceCategory.CAFE, null
            );

            // then - 리뷰 0개는 통과, 평균 4.5 통과, 평균 2.0 제외
            assertThat(result).hasSize(2);
            assertThat(result).extracting(PlaceSummaryResponse::name)
                    .containsExactlyInAnyOrder("리뷰없는 카페", "별점높은 카페");
        }
    }

    @Nested
    @DisplayName("비슷한 성향 공간 조회 (/similar)")
    class SimilarPlacesTest {

        @Test
        void 비슷한_성향_공간_유저_태그_기반_추천() {
            // given
            Tag quiet = saveTag("조용한");
            Tag spacious = saveTag("넓은");
            Tag manyOutlets = saveTag("콘센트 많은");

            Place wishedPlace = savePlaceWithTags("찜한 카페", 126.9769, 37.5759, quiet, spacious);
            saveWishlist(user, wishedPlace);

            savePlaceWithTags("후보1", 126.9800, 37.5770, quiet, spacious);
            savePlaceWithTags("후보2", 126.9850, 37.5780, manyOutlets);

            entityManager.flush();
            entityManager.clear();

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                    user.getId(), 11010L, PlaceCategory.CAFE, 126.978, 37.570
            );

            // then
            assertThat(result).hasSizeGreaterThan(0);
            assertThat(result.getFirst().name()).isEqualTo("후보1");
        }

        @Test
        void 비슷한_성향_공간_그룹_태그_기반_추천() {
            // given
            User groupUser1 = createGroupUser("그룹유저1", "group_001", 30);
            User groupUser2 = createGroupUser("그룹유저2", "group_002", 32);

            Tag quiet = saveTag("조용한");
            Tag emotional = saveTag("감성적인");
            Tag reviewTag = tagRepository.save(Tag.of(TagType.REVIEW, "작업하기 좋은"));

            Place myPlace = savePlaceWithTags("내 찜", 126.9769, 37.5759, quiet);
            Place groupFavorite = savePlaceWithTags("그룹 선호", 126.9800, 37.5770, emotional);
            Place candidate = savePlaceWithTags("후보", 126.9850, 37.5780, emotional);

            saveWishlist(user, myPlace);
            saveWishlist(groupUser1, groupFavorite);
            saveWishlist(groupUser2, groupFavorite);

            Review review = createReview(user, myPlace, 5);
            reviewTagRepository.save(ReviewTag.of(review, reviewTag));

            entityManager.flush();
            entityManager.clear();

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                    user.getId(), 11010L, PlaceCategory.CAFE, 126.978, 37.570
            );

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.stream().map(PlaceSummaryResponse::name)).contains("후보");
        }

        @Test
        void 비슷한_성향_공간_찜하거나_리뷰한_장소_제외() {
            // given
            Tag quiet = saveTag("조용한");

            Place wishedPlace = savePlaceWithTags("찜한 카페", 126.9769, 37.5759, quiet);
            Place reviewedPlace = savePlaceWithTags("리뷰한 카페", 126.9800, 37.5770, quiet);
            savePlaceWithTags("후보 카페", 126.9850, 37.5780, quiet);

            saveWishlist(user, wishedPlace);
            createReview(user, reviewedPlace, 4);

            entityManager.flush();
            entityManager.clear();

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                    user.getId(), 11010L, PlaceCategory.CAFE, 126.978, 37.570
            );

            // then
            assertThat(result.stream().map(PlaceSummaryResponse::name))
                    .doesNotContain("찜한 카페", "리뷰한 카페")
                    .contains("후보 카페");
        }

        @Test
        void 비슷한_성향_공간_거리_감쇠_적용() {
            // given
            Tag quiet = saveTag("조용한");

            Place wishedPlace = savePlaceWithTags("기준", 126.9769, 37.5759, quiet);
            savePlaceWithTags("가까운 카페", 126.9780, 37.5760, quiet);
            savePlaceWithTags("먼 카페", 126.9950, 37.5850, quiet);

            saveWishlist(user, wishedPlace);
            entityManager.flush();
            entityManager.clear();

            // when
            List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                    user.getId(), 11010L, PlaceCategory.CAFE, 126.978, 37.576
            );

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().name()).isEqualTo("가까운 카페");
        }

        @Test
        void 비슷한_성향_공간_태그_없으면_빈_리스트() {
            // given
            savePlace("카페1", PlaceCategory.CAFE, 126.9769, 37.5759, 1101005200L);
            savePlace("카페2", PlaceCategory.CAFE, 126.9800, 37.5770, 1101005200L);

            // when - 찜도 리뷰도 없음
            List<PlaceSummaryResponse> result = placeRecommendationService.getSimilarPlaces(
                    user.getId(), 11010L, PlaceCategory.CAFE, 126.978, 37.570
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    private Place savePlaceWithDetails(
            String name, PlaceCategory category,
            double longitude, double latitude, long regionCode,
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
                            double longitude, double latitude, long regionCode) {
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

    private Tag saveTag(String name) {
        return tagRepository.save(Tag.of(TagType.PLACE, name));
    }

    private Place savePlaceWithTags(String name, double longitude, double latitude, Tag... tags) {
        Place place = savePlace(name, PlaceCategory.CAFE, longitude, latitude, 1101005200L);
        for (Tag tag : tags) {
            placeTagRepository.save(PlaceTag.of(place, tag));
        }
        return place;
    }

    private User createGroupUser(String name, String providerId, int age) {
        return userRepository.save(User.builder()
                .name(name)
                .nickname(name + "닉네임")
                .gender(Gender.MALE)
                .age(age)
                .provider(Provider.KAKAO)
                .providerId(providerId)
                .role(UserRole.USER)
                .build());
    }

    private Review createReview(User user, Place place, int rating) {
        Review review = Review.builder()
                .user(user)
                .place(place)
                .rating(rating)
                .build();
        ReflectionTestUtils.setField(review, "status", ReviewStatus.ACTIVE);
        return reviewRepository.save(review);
    }

    private void saveWishlist(User user, Place place) {
        wishlistRepository.save(Wishlist.of(user, place));
    }
}
