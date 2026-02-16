package io.dnd.goyo.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HalfStepValidator implements ConstraintValidator<HalfStep, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        double doubled = value * 2;
        return doubled == Math.floor(doubled);
    }
}
