package paxos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LoggerUtilTest.java
 * -------------------
 * Tests for the LoggerUtil utility class.
 * These tests verify that all log methods print formatted output
 * containing timestamps, member IDs, and expected symbols.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class LoggerUtilTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams() {
        // Redirect system output to capture printed logs
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testLogMethodPrintsFormattedOutput() {
        LoggerUtil.log("M1", "Testing log message");
        String output = outContent.toString();

        assertTrue(output.contains("M1"), "Output should include member ID.");
        assertTrue(output.contains("Testing log message"), "Output should include the log message.");
        assertTrue(output.matches("(?s).*\\[\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\].*"), "Output should contain a timestamp.");
    }

    @Test
    void testBannerPrintsHeaderFormat() {
        LoggerUtil.banner("Scenario 1");
        String output = outContent.toString();

        assertTrue(output.contains("SCENARIO 1"), "Banner should display title in uppercase.");
        assertTrue(output.contains("="), "Banner should include separator lines.");
    }

    @Test
    void testAlertMethodIncludesWarningSymbols() {
        LoggerUtil.alert("M3", "Consensus reached");
        String output = outContent.toString();

        assertTrue(output.contains("⚠"), "Alert output should include warning symbols.");
        assertTrue(output.contains("Consensus reached"), "Alert should include message text.");
        assertTrue(output.contains("M3"), "Alert should include member ID.");
    }

    @Test
    void testNoExceptionsThrownByLoggingMethods() {
        assertDoesNotThrow(() -> LoggerUtil.log("M1", "Safe call"));
        assertDoesNotThrow(() -> LoggerUtil.alert("M2", "No crash"));
        assertDoesNotThrow(() -> LoggerUtil.banner("Final Test"));
    }
}
