package io.dnd.goyo.domain.placetag.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.tag.entity.Tag;
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
@Table(name = "place_tags", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_place_tag_place_tag",
                columnNames = {"place_id", "tag_id"}
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static PlaceTag of(Place place, Tag tag) {
        return new PlaceTag(place, tag);
    }

    private PlaceTag(Place place, Tag tag) {
        validatePlace(place);
        validateTag(tag);

        this.place = place;
        this.tag = tag;
    }

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "공간 정보는 필수입니다.");
        }
    }

    private static void validateTag(Tag tag) {
        if (tag == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 정보는 필수입니다.");
        }
    }
}
