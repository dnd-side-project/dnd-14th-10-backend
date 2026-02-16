package io.dnd.goyo.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class HalfStepValidator implements ConstraintValidator<HalfStep, BigDecimal> {

    private static final BigDecimal HALF = new BigDecimal("0.5");

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.remainder(HALF).compareTo(BigDecimal.ZERO) == 0;
    }
}
