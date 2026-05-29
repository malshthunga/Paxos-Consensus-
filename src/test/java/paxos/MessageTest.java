package paxos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageTest.java
 * ----------------
 * Unit tests for the Message class in the Paxos Council project.
 * These tests verify message creation, serialization, deserialization,
 * and string formatting to ensure correct communication between nodes.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class MessageTest {

    /**
     * Tests that the Message constructor correctly assigns all fields.
     * This ensures that when a proposer or acceptor creates a new message,
     * it carries the right metadata (type, sender, proposal number, candidate).
     */
    @Test
    void testMessageCreation() {
        Message msg = new Message("PREPARE", "M1", 10, "M5");

        // Verify that the constructor values are correctly assigned
        assertEquals("PREPARE", msg.getType());
        assertEquals("M1", msg.getSender());
        assertEquals(10, msg.getProposalNum());
        assertEquals("M5", msg.getCandidate());
    }

    /**
     * Tests the no-argument constructor which is required for Gson deserialization.
     * Default values should be blank or -1 to indicate uninitialized fields.
     */
    @Test
    void testDefaultConstructor() {
        Message msg = new Message();

        // Verify default field values
        assertEquals("", msg.getType());
        assertEquals("", msg.getSender());
        assertEquals(-1, msg.getProposalNum());
        assertEquals("", msg.getCandidate());
    }

    /**
     * Tests JSON conversion using Gson.
     * Ensures that toJson() and fromJson() produce equivalent Message objects.
     * This is critical for reliable network communication between council members.
     */
    @Test
    void testToJsonAndFromJson() {
        Message original = new Message("ACCEPT", "M3", 7, "M8");

        // Convert to JSON text
        String json = original.toJson();

        // Deserialize the text back into a Message object
        Message reconstructed = Message.fromJson(json);

        // The reconstructed message must match the original
        assertEquals(original.getType(), reconstructed.getType());
        assertEquals(original.getSender(), reconstructed.getSender());
        assertEquals(original.getProposalNum(), reconstructed.getProposalNum());
        assertEquals(original.getCandidate(), reconstructed.getCandidate());
    }

    /**
     * Tests the human-readable string representation of the message.
     * This is used mainly for debugging and logging output.
     */
    @Test
    void testToStringFormat() {
        Message msg = new Message("PROMISE", "M2", 15, "M4");

        // Convert message to string
        String text = msg.toString();

        // Check that all key components appear in the log text
        assertTrue(text.contains("TYPE:PROMISE"));
        assertTrue(text.contains("SENDER:M2"));
        assertTrue(text.contains("PROPOSAL:15"));
        assertTrue(text.contains("CANDIDATE:M4"));
    }
}
