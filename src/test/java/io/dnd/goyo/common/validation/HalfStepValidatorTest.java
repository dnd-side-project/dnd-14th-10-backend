package io.dnd.goyo.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HalfStepValidatorTest {

    private final HalfStepValidator validator = new HalfStepValidator();

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"})
    @DisplayName("0.5 단위 값은 유효하다")
    void 유효한_0점5_단위_값(String value) {
        assertThat(validator.isValid(new BigDecimal(value), null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.3", "1.2", "2.7", "4.9", "0.1", "3.3"})
    @DisplayName("0.5 단위가 아닌 값은 무효하다")
    void 무효한_값(String value) {
        assertThat(validator.isValid(new BigDecimal(value), null)).isFalse();
    }

    @Test
    @DisplayName("null 값은 유효하다")
    void null_값은_유효하다() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
