package io.dnd.goyo.domain.review.entity;

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
@Table(name = "review_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private boolean isPrimary;

    public static ReviewImage of(Review review, String imageUrl, int sequence, boolean isPrimary) {
        return new ReviewImage(review, imageUrl, sequence, isPrimary);
    }

    private ReviewImage(Review review, String imageUrl, int sequence, boolean isPrimary) {
        validateReview(review);
        validateImageUrl(imageUrl);
        validateSequence(sequence);

        this.review = review;
        this.imageUrl = imageUrl;
        this.sequence = sequence;
        this.isPrimary = isPrimary;
    }

    private static void validateReview(Review review) {
        if (review == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "리뷰 정보는 필수입니다.");
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
