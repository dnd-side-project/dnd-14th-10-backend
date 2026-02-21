package io.dnd.goyo.domain.wishlist.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum WishlistSortType {
    LATEST("createdAt", Sort.Direction.DESC),
    NAME("place.name", Sort.Direction.ASC),
    POPULAR(null, null);

    private final String property;
    private final Sort.Direction direction;

    WishlistSortType(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public Sort toSort() {
        if (this == POPULAR) {
            return Sort.unsorted();
        }
        if (this == NAME) {
            return Sort.by(
                    new Sort.Order(direction, property),
                    new Sort.Order(Sort.Direction.DESC, "createdAt")
            );
        }
        return Sort.by(direction, property);
    }
}
