package io.dnd.goyo.domain.review.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum ReviewSortType {
    LATEST("createdAt", Sort.Direction.DESC),
    NAME("place.name", Sort.Direction.ASC);

    private final String property;
    private final Sort.Direction direction;

    ReviewSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public Sort toSort() {
        if (this == NAME) {
            return Sort.by(
                    new Sort.Order(direction, property),
                    new Sort.Order(Sort.Direction.DESC, "createdAt")
            );
        }
        return Sort.by(direction, property);
    }
}
