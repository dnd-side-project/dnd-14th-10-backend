package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
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
@Table(name = "place_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private boolean representativeFlag;

    @Column(nullable = false)
    private int sequence;

    public static PlaceImage of(
            Place place,
            String imageUrl,
            boolean representativeFlag,
            int sequence
    ) {
        return new PlaceImage(place, imageUrl, representativeFlag, sequence);
    }

    private PlaceImage(
            Place place,
            String imageUrl,
            boolean representativeFlag,
            int sequence
    ) {
        validatePlace(place);
        validateImageUrl(imageUrl);
        validateSequence(sequence);

        this.place = place;
        this.imageUrl = imageUrl;
        this.representativeFlag = representativeFlag;
        this.sequence = sequence;
    }

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 정보는 필수입니다.");
        }
    }

    private static void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 URL은 필수입니다.");
        }
    }

    private static void validateSequence(int sequence) {
        if (sequence < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 순서는 0 이상이어야 합니다.");
        }
    }
}
