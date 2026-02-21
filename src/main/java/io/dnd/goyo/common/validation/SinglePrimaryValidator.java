package io.dnd.goyo.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class SinglePrimaryValidator implements ConstraintValidator<SinglePrimary, List<? extends Primaryable>> {

    @Override
    public boolean isValid(List<? extends Primaryable> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        long primaryCount = value.stream().filter(Primaryable::isPrimary).count();
        return primaryCount <= 1;
    }
}
