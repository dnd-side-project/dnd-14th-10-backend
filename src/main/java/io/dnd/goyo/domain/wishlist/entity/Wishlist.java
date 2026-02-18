package io.dnd.goyo.domain.wishlist.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
    @UniqueConstraint(name = "uk_wishlist_user_place", columnNames = {"user_id", "place_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public static Wishlist of(User user, Place place) {
        return new Wishlist(user, place);
    }

    private Wishlist(User user, Place place) {
        validateUser(user);
        validatePlace(place);

        this.user = user;
        this.place = place;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 정보가 누락되었습니다.");
        }
    }

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "공간 정보가 누락되었습니다.");
        }
    }
}
