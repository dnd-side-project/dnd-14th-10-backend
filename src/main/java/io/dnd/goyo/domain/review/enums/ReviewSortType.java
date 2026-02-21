package io.dnd.goyo.domain.review.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum ReviewSortType {
    LATEST("createdAt", Sort.Direction.DESC) {
        @Override
        public Sort toSort() {
            return Sort.by(getDirection(), getProperty());
        }
    },
    NAME("place.name", Sort.Direction.ASC) {
        @Override
        public Sort toSort() {
            return Sort.by(
                    new Sort.Order(getDirection(), getProperty()),
                    new Sort.Order(Sort.Direction.DESC, "createdAt")
            );
        }
    };

    private final String property;
    private final Sort.Direction direction;

    ReviewSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public abstract Sort toSort();
}
