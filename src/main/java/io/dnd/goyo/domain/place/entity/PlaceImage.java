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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_images", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_place_image_sequence",
                columnNames = {"place_id", "sequence"}
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private boolean representativeFlag;

    @Column(nullable = false)
    private int sequence;

    public static PlaceImage of(
            String imageKey,
            boolean representativeFlag,
            int sequence
    ) {
        return new PlaceImage(imageKey, representativeFlag, sequence);
    }

    private PlaceImage(
            String imageKey,
            boolean representativeFlag,
            int sequence
    ) {
        validateImageKey(imageKey);
        validateSequence(sequence);

        this.imageKey = imageKey;
        this.representativeFlag = representativeFlag;
        this.sequence = sequence;
    }

    void assignPlace(Place place) {
        this.place = place;
    }

    private static void validateImageKey(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 키는 필수입니다.");
        }
    }

    private static void validateSequence(int sequence) {
        if (sequence < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 순서는 0 이상이어야 합니다.");
        }
    }
}
