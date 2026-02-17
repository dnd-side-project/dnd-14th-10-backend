package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagService;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
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
    private final UserReader userReader;
    private final GeometryUtils geometryUtils;
    private final ApplicationEventPublisher eventPublisher;

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
}
