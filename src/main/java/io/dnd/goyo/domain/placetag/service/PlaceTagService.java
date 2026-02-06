package io.dnd.goyo.domain.placetag.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.placetag.entity.PlaceTag;
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
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<Long> uniqueTagIds = tagIds.stream()
                .distinct()
                .toList();

        List<Tag> tags = findAndValidateTags(uniqueTagIds);

        List<PlaceTag> placeTags = tags.stream()
                .map(tag -> PlaceTag.of(place, tag))
                .toList();

        placeTagRepository.saveAll(placeTags);
    }

    private List<Tag> findAndValidateTags(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 태그가 포함되어 있습니다.");
        }
        return tags;
    }
}
