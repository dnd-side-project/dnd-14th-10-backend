package io.dnd.goyo.domain.place.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum PlaceSortType {
    LATEST("createdAt", Sort.Direction.DESC) {
        @Override
        public Sort toSort() {
            return Sort.by(getDirection(), getProperty());
        }
    },
    NAME("name", Sort.Direction.ASC) {
        @Override
        public Sort toSort() {
            return Sort.by(
                    new Sort.Order(getDirection(), getProperty()),
                    new Sort.Order(Sort.Direction.DESC, "createdAt")
            );
        }
    },
    POPULAR(null, null) {
        @Override
        public Sort toSort() {
            return Sort.unsorted();
        }
    };

    private final String property;
    private final Sort.Direction direction;

    PlaceSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public abstract Sort toSort();
}
