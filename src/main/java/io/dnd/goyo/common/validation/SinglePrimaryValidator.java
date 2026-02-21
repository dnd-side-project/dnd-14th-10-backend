package io.dnd.goyo.common.validation;

import io.dnd.goyo.domain.review.dto.request.ReviewImageRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class SinglePrimaryValidator implements ConstraintValidator<SinglePrimary, List<ReviewImageRequest>> {

    @Override
    public boolean isValid(List<ReviewImageRequest> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        long primaryCount = value.stream().filter(ReviewImageRequest::isPrimary).count();
        return primaryCount <= 1;
    }
}
