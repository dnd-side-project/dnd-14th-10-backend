package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceDetailService {

    private final PlaceDetailRepository placeDetailRepository;

    @Transactional
    public void registerPlaceDetail(PlaceDetail placeDetail) {
        placeDetailRepository.save(placeDetail);
    }

    public void updateScore(PlaceDetail placeDetail, Mood mood, SpaceSize spaceSize, OutletScore outletScore, CrowdStatus crowdStatus) {
        if (mood == null && spaceSize == null && outletScore == null && crowdStatus == null) {
            return;
        }
        placeDetail.updateScore(mood, spaceSize, outletScore, crowdStatus);
    }
}
