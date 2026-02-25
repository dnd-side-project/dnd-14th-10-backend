package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
class PlaceServiceIntegrationTest {

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
    private PlaceService placeService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PlaceDetailRepository placeDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private GeometryFactory geometryFactory;

    @MockitoBean
    private FileStorage fileStorage;

    @Autowired
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        given(fileStorage.generatePublicUrl(anyString())).willAnswer(inv -> inv.getArgument(0));

        user = userRepository.save(User.builder()
                .name("테스트유저")
                .nickname("테스트닉네임")
                .gender(Gender.MALE)
                .age(30)
                .provider(Provider.KAKAO)
                .providerId("test_001")
                .role(UserRole.USER)
                .build());

        userStatsRepository.save(UserStats.of(user));
    }

    @Test
    @DisplayName("공간 삭제 시 찜이 있어도 status가 DELETED로 변경되어야 한다")
    void deletePlace_withWishlist_statusShouldBeDeleted() {
        // given
        Place place = savePlace("테스트 카페");
        wishlistRepository.save(Wishlist.of(user, place));
        entityManager.flush();
        entityManager.clear();

        // when
        placeService.deletePlace(user.getId(), place.getId());

        // then - 영속성 컨텍스트가 clear된 상태이므로 DB에서 직접 조회
        PlaceStatus status = entityManager
                .createQuery("SELECT p.status FROM Place p WHERE p.id = :id", PlaceStatus.class)
                .setParameter("id", place.getId())
                .getSingleResult();

        assertThat(status).isEqualTo(PlaceStatus.DELETED);
    }

    private Place savePlace(String name) {
        Place place = Place.builder()
                .name(name)
                .category(PlaceCategory.CAFE)
                .location(geometryFactory.createPoint(new Coordinate(126.9769, 37.5759)))
                .regionCode(1101005200L)
                .addressDetail("테스트 주소")
                .user(user)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        place.addImages(List.of(PlaceImage.of("places/test.jpg", true, 0)));
        placeRepository.saveAndFlush(place);
        placeDetailRepository.saveAndFlush(PlaceDetail.of(place, 50, 50, 50, 50));
        return place;
    }
}
