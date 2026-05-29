package paxos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageTypeTest.java
 * ---------------------
 * Unit tests for the MessageType enumeration in the Paxos Council project.
 * Ensures that all defined message types exist and can be used reliably
 * for comparison and serialization throughout the Paxos algorithm.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class MessageTypeTest {

    /**
     * Verifies that all expected enum constants are defined correctly.
     * This helps prevent typos or missing message types in communication logic.
     */
    @Test
    void testEnumContainsAllExpectedValues() {
        // Retrieve all defined message types
        MessageType[] types = MessageType.values();

        // Ensure the correct number of message types exist
        assertEquals(8, types.length, "MessageType enum should have 8 constants");

        // Verify that key message phases exist
        assertTrue(contains(types, MessageType.PREPARE));
        assertTrue(contains(types, MessageType.PROMISE));
        assertTrue(contains(types, MessageType.ACCEPT_REQUEST));
        assertTrue(contains(types, MessageType.ACCEPTED));
        assertTrue(contains(types, MessageType.DECIDE));
        assertTrue(contains(types, MessageType.REJECT));
        assertTrue(contains(types, MessageType.LEARN));
        assertTrue(contains(types, MessageType.HEARTBEAT));
    }

    /**
     * Tests that the toString() output matches the enum name.
     * This is useful when converting message types to text for logs or JSON.
     */
    @Test
    void testToStringMatchesName() {
        assertEquals("PREPARE", MessageType.PREPARE.toString());
        assertEquals("PROMISE", MessageType.PROMISE.toString());
        assertEquals("HEARTBEAT", MessageType.HEARTBEAT.toString());
    }

    /**
     * Helper function to check whether an enum array contains a specific constant.
     */
    private boolean contains(MessageType[] arr, MessageType value) {
        for (MessageType t : arr) {
            if (t == value) return true;
        }
        return false;
    }
}
