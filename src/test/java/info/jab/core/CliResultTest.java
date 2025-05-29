package info.jab.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CliResult enum.
 */
class CliResultTest {

    @Test
    @DisplayName("Should have OK with exit code 1")
    void should_haveOKWithExitCode1() {
        // When & Then
        assertThat(CliResult.OK.getExitCode())
            .as("CliResult.OK should have exit code 1")
            .isOne();
    }

    @Test
    @DisplayName("Should have KO with exit code 0")
    void should_haveKOWithExitCode0() {
        // When & Then
        assertThat(CliResult.KO.getExitCode())
            .as("CliResult.KO should have exit code 0")
            .isZero();
    }

    @Test
    @DisplayName("Should have only two enum values")
    void should_haveOnlyTwoEnumValues() {
        // When & Then
        assertThat(CliResult.values())
            .as("CliResult should have exactly two values")
            .hasSize(2)
            .containsExactly(CliResult.OK, CliResult.KO);
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void should_haveProperToStringRepresentation() {
        // When & Then
        assertThat(CliResult.OK.toString())
            .as("CliResult.OK toString should be 'OK'")
            .isEqualTo("OK");

        assertThat(CliResult.KO.toString())
            .as("CliResult.KO toString should be 'KO'")
            .isEqualTo("KO");
    }

    @Test
    @DisplayName("Should have proper name values")
    void should_haveProperNameValues() {
        // When & Then
        assertThat(CliResult.OK.name())
            .as("CliResult.OK name should be 'OK'")
            .isEqualTo("OK");

        assertThat(CliResult.KO.name())
            .as("CliResult.KO name should be 'KO'")
            .isEqualTo("KO");
    }

    @Test
    @DisplayName("Should support valueOf operations")
    void should_supportValueOfOperations() {
        // When & Then
        assertThat(CliResult.valueOf("OK"))
            .as("valueOf('OK') should return CliResult.OK")
            .isEqualTo(CliResult.OK);

        assertThat(CliResult.valueOf("KO"))
            .as("valueOf('KO') should return CliResult.KO")
            .isEqualTo(CliResult.KO);
    }
}
