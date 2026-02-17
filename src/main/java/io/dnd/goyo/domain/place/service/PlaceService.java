package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagService;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceDetailService placeDetailService;
    private final PlaceTagService placeTagService;
    private final UserReader userReader;
    private final WishlistReader wishlistReader;
    private final GeometryUtils geometryUtils;
    private final FileStorage fileStorage;

    @Transactional
    public Long registerPlace(Long userId, PlaceRegisterRequest request) {
        User user = userReader.getUser(userId);
        Point location = geometryUtils.createPoint(request.longitude(), request.latitude());

        Place place = request.toPlaceEntity(user, location);
        place.addImages(request.toImageEntities());
        placeRepository.save(place);

        PlaceDetail placeDetail = request.toPlaceDetailEntity(place);
        placeDetailService.registerPlaceDetail(placeDetail);
        placeTagService.registerPlaceTags(place, request.tagIds());

        return place.getId();
    }

    public PlaceDetailResponse getPlaceDetail(Long userId, Long placeId) {
        Place place = placeRepository.findByIdWithDetails(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        boolean isWished = false;
        if (userId != null) {
            isWished = wishlistReader.isWished(userId, placeId);
        }

        return PlaceDetailResponse.from(place, isWished, fileStorage);
    }
}
