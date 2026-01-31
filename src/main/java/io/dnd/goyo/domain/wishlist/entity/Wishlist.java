package io.dnd.goyo.domain.wishlist.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long id;

    // TODO: [User 엔티티 머지 후] User로 변경
    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public static Wishlist of(Long userId, Place place) {
        return new Wishlist(userId, place);
    }

    private Wishlist(Long userId, Place place) {
        validateUserId(userId);
        validatePlace(place);

        this.userId = userId;
        this.place = place;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "사용자 정보가 누락되었습니다.");
        }
    }

    private void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "장소 정보가 누락되었습니다.");
        }
    }
}
