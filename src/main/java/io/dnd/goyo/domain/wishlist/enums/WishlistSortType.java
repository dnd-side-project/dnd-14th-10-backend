package io.dnd.goyo.domain.wishlist.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum WishlistSortType {
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
    },
    POPULAR(null, null) {
        @Override
        public Sort toSort() {
            return Sort.unsorted();
        }
    };

    private final String property;
    private final Sort.Direction direction;

    WishlistSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public abstract Sort toSort();
}
