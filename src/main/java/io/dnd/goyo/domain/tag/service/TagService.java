package io.dnd.goyo.domain.tag.service;

import io.dnd.goyo.domain.tag.dto.response.TagResponse;
import io.dnd.goyo.domain.tag.enums.TagType;
import io.dnd.goyo.domain.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getTagsByType(TagType type) {
        return tagRepository.findAllByType(type).stream()
                .map(TagResponse::from)
                .toList();
    }
}
