package io.dnd.goyo.domain.review.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.tag.entity.Tag;
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
@Table(name = "review_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static ReviewTag of(Review review, Tag tag) {
        return new ReviewTag(review, tag);
    }

    private ReviewTag(Review review, Tag tag) {
        validateReview(review);
        validateTag(tag);

        this.review = review;
        this.tag = tag;
    }

    private static void validateReview(Review review) {
        if (review == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "리뷰 정보는 필수입니다.");
        }
    }

    private static void validateTag(Tag tag) {
        if (tag == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 정보는 필수입니다.");
        }
    }
}
