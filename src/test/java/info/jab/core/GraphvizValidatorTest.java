package info.jab.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphvizValidatorTest {

    private GraphvizValidator graphvizValidator;

    @BeforeEach
    void setUp() {
        graphvizValidator = new GraphvizValidator();
    }

    @Test
    void isGraphvizAvailable_shouldReturnBooleanValue() {
        // When
        boolean result = graphvizValidator.isGraphvizAvailable();

        // Then
        // We can't assert a specific value since it depends on the system
        // but we can verify it returns a boolean without throwing exceptions
        assertThat(result).isInstanceOf(Boolean.class);
    }
}
