package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.request.PlaceImageRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagService;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import java.time.LocalTime;
import java.util.List;
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
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceDetailService placeDetailService;

    @Mock
    private PlaceTagService placeTagService;

    @Mock
    private UserReader userReader;

    @Mock
    private GeometryUtils geometryUtils;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private static PlaceRegisterRequest createRegisterRequest() {
        return new PlaceRegisterRequest(
                "테스트 카페",
                PlaceCategory.CAFE,
                37.5,
                127.0,
                2,
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                11111,
                "가온로 245",
                "1층",
                OutletScore.MANY,
                SpaceSize.LARGE,
                CrowdStatus.RELAX,
                Mood.CALM,
                List.of(1L, 2L),
                List.of(new PlaceImageRequest("image.jpg", 0, true))
        );
    }

    @Test
    void 장소_등록_성공() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
        PlaceRegisterRequest request = createRegisterRequest();

        given(userReader.getUser(userId)).willReturn(user);
        given(geometryUtils.createPoint(request.longitude(), request.latitude())).willReturn(location);

        // when
        placeService.registerPlace(userId, request);

        // then
        verify(userReader).getUser(userId);
        verify(geometryUtils).createPoint(request.longitude(), request.latitude());
        verify(placeRepository).save(any(Place.class));
        verify(placeDetailService).registerPlaceDetail(any(PlaceDetail.class));
        verify(placeTagService).registerPlaceTags(any(Place.class), eq(request.tagIds()));
    }

    @Test
    void 존재하지_않는_사용자로_등록_시_예외_발생() {
        // given
        Long userId = 999L;
        PlaceRegisterRequest request = createRegisterRequest();

        given(userReader.getUser(userId))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> placeService.registerPlace(userId, request))
                .isInstanceOf(BusinessException.class);

        verify(placeRepository, never()).save(any(Place.class));
    }
}
