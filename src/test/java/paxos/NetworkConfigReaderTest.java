package paxos;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NetworkConfigReaderTest.java
 * ----------------------------
 * Unit tests for the NetworkConfigReader class.
 * These tests verify that configuration files are parsed correctly,
 * with valid entries stored and invalid/commented lines ignored.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class NetworkConfigReaderTest {

    private File tempConfigFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file to simulate network.config
        tempConfigFile = File.createTempFile("network", ".config");

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempConfigFile))) {
            writer.println("# Paxos network configuration");
            writer.println("M1, localhost, 9001");
            writer.println("M2, 127.0.0.1, 9002");
            writer.println("M3, localhost, 9003");
            writer.println(""); // blank line
            writer.println("InvalidLineWithoutCommas");
        }
    }

    @AfterEach
    void tearDown() {
        if (tempConfigFile.exists()) {
            tempConfigFile.delete();
        }
        NetworkConfigReader.ports.clear(); // reset static state
    }

    /**
     * Test that loadNetworkConfig correctly parses valid entries
     * and ignores commented/invalid lines.
     */
    @Test
    void testLoadNetworkConfigParsesValidEntries() throws IOException {
        Map<String, InetSocketAddress> result =
                NetworkConfigReader.loadNetworkConfig(tempConfigFile.getAbsolutePath());

        // Verify number of valid entries (3 valid lines)
        assertEquals(3, result.size(), "Should load 3 valid member entries.");

        // Check that each entry maps to the correct host and port
        assertEquals(9001, result.get("M1").getPort());
        assertEquals(9002, result.get("M2").getPort());
        assertEquals(9003, result.get("M3").getPort());

        // Check the ports static map
        assertEquals(3, NetworkConfigReader.ports.size(), "Ports map should also contain 3 entries.");
        assertTrue(NetworkConfigReader.ports.containsKey("M1"));
    }

    /**
     * Test that invalid lines are skipped and do not cause exceptions.
     */
    @Test
    void testLoadNetworkConfigSkipsInvalidLines() throws IOException {
        assertDoesNotThrow(() ->
                        NetworkConfigReader.loadNetworkConfig(tempConfigFile.getAbsolutePath()),
                "Invalid lines should be ignored, not crash the parser.");
    }

    /**
     * Test that the method handles an empty file gracefully.
     */
    @Test
    void testLoadNetworkConfigWithEmptyFile() throws IOException {
        // Create an empty file
        File emptyFile = File.createTempFile("empty", ".config");

        Map<String, InetSocketAddress> result =
                NetworkConfigReader.loadNetworkConfig(emptyFile.getAbsolutePath());

        assertTrue(result.isEmpty(), "Empty config should produce an empty map.");
        emptyFile.delete();
    }
}
