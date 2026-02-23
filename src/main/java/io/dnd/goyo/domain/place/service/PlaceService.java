package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceUpdateRequest;
import io.dnd.goyo.domain.place.dto.response.MyPlaceResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceMapItemResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.PlaceSortType;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagService;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.history.event.PlaceViewedEvent;
import io.dnd.goyo.domain.user.entity.User;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import io.dnd.goyo.domain.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceDetailService placeDetailService;
    private final PlaceTagService placeTagService;
    private final PlaceImageService placeImageService;
    private final UserReader userReader;
    private final WishlistReader wishlistReader;
    private final WishlistService wishlistService;
    private final GeometryUtils geometryUtils;
    private final ApplicationEventPublisher eventPublisher;
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

        eventPublisher.publishEvent(new ActivityEvent(userId, ActivityType.PLACE, 1, 1));

        return place.getId();
    }

    public PlaceDetailResponse getPlaceDetail(Long userId, Long placeId) {
        Place place = placeRepository.findByIdWithDetails(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        boolean isWished = false;
        if (userId != null) {
            isWished = wishlistReader.isWished(userId, placeId);
        }

        if (userId != null) {
            eventPublisher.publishEvent(new PlaceViewedEvent(userId, placeId));
        }

        return PlaceDetailResponse.from(place, isWished, fileStorage);
    }

    @Transactional
    public void updatePlace(Long userId, Long placeId, PlaceUpdateRequest request) {
        Place place = placeRepository.findByIdWithDetails(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        validateOwner(userId, place);
        place.update(request.name(), request.floorInfo(), request.openTime(), request.closeTime(), request.restroomInfo());
        placeDetailService.updateScore(place.getPlaceDetail(), request.mood(), request.spaceSize(), request.outletScore(), request.crowdStatus());
        placeTagService.replacePlaceTags(place, request.tagIds());
        placeImageService.replaceImages(place, placeId, request.images());
    }

    @Transactional
    public void deletePlace(Long userId, Long placeId) {
        Place place = placeRepository.findByIdWithDetails(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));

        validateOwner(userId, place);
        placeImageService.deleteAllImages(place, placeId);
        place.delete();
        wishlistService.deleteByPlaceId(placeId);

        eventPublisher.publishEvent(new ActivityEvent(userId, ActivityType.PLACE, -1, -1));
    }

    public Page<MyPlaceResponse> getMyPlaces(Long userId, Pageable pageable, PlaceSortType sortType) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Place> placePage;
        if (sortType == PlaceSortType.POPULAR) {
            placePage = placeRepository.findAllByUserIdOrderByWishCountDesc(userId, pageRequest);
        } else {
            placePage = placeRepository.findAllByUserIdAndStatus(userId, PlaceStatus.ACTIVE, pageRequest.withSort(sortType.toSort()));
        }

        List<Long> placeIds = placePage.map(Place::getId).toList();
        Set<Long> wishedPlaceIds = wishlistReader.getWishedPlaceIdSet(userId, placeIds);

        return placePage.map(place ->
                MyPlaceResponse.from(place, wishedPlaceIds.contains(place.getId()), fileStorage));
    }

    public List<PlaceMapItemResponse> getPlacesByIds(List<Long> ids) {
        List<Place> places = placeRepository.findAllByIdWithDetails(ids);

        return places.stream()
                .map(this::toMapItemResponse)
                .toList();
    }

    private PlaceMapItemResponse toMapItemResponse(Place place) {
        return PlaceMapItemResponse.of(
                place,
                place.getPlaceDetail(),
                fileStorage
        );
    }

    private void validateOwner(Long userId, Place place) {
        boolean isOwner = place.getUser().getId().equals(userId);
        if (!isOwner) {
            throw new BusinessException(ErrorCode.PLACE_NOT_OWNER);
        }
    }
}
