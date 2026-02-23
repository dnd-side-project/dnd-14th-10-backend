package io.dnd.goyo.domain.placetag.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.placetag.entity.PlaceTag;
import io.dnd.goyo.domain.placetag.entity.PlaceTags;
import io.dnd.goyo.domain.placetag.repository.PlaceTagRepository;
import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceTagService {

    private final PlaceTagRepository placeTagRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void registerPlaceTags(Place place, List<Long> tagIds) {
        PlaceTags placeTags = PlaceTags.from(tagIds);

        List<Tag> tags = findAndValidateTags(placeTags.tagIds());

        List<PlaceTag> placeTagList = tags.stream()
                .map(tag -> PlaceTag.of(place, tag))
                .toList();

        placeTagRepository.saveAll(placeTagList);
    }

    @Transactional
    public void replacePlaceTags(Place place, List<Long> tagIds) {
        placeTagRepository.deleteAllByPlaceId(place.getId());
        registerPlaceTags(place, tagIds);
    }

    private List<Tag> findAndValidateTags(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 태그가 포함되어 있습니다.");
        }
        return tags;
    }
}
