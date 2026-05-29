package paxos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * LearnerTest.java
 * ----------------
 * Unit tests for the Learner class in the Paxos Council project.
 * These tests verify how the Learner counts accepted votes, detects consensus,
 * broadcasts DECIDE messages, and synchronizes the final decision value.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class LearnerTest {

    private Learner learner;
    private MockCouncilMember mockMember;

    /**
     * MockCouncilMember is a lightweight substitute for CouncilMember
     * that overrides quorum size and broadcast behavior for testing
     * without sockets or threads.
     */
    static class MockCouncilMember extends CouncilMember {
        private final List<String> broadcastedMessages = new ArrayList<>();

        public MockCouncilMember(String id) {
            super(id, "reliable", new HashMap<>());
        }

        @Override
        public void broadcast(String json) {
            broadcastedMessages.add(json);
        }

        @Override
        public int quorumSize() {
            return 3; // small majority for testing
        }

        public List<String> getBroadcastedMessages() {
            return broadcastedMessages;
        }
    }

    @BeforeEach
    void setUp() {
        mockMember = new MockCouncilMember("M1");
        learner = new Learner(mockMember);
        CouncilMember.decidedValue = null; // reset shared state
    }

    /**
     * Test that the Learner registers accepted votes correctly and
     * triggers consensus once a quorum is reached.
     */
    @Test
    void testLearnerReachesConsensusAfterQuorum() {
        // Simulate 3 ACCEPTED messages for the same candidate
        learner.handleAccepted("M2", 10, "M5");
        learner.handleAccepted("M3", 10, "M5");
        learner.handleAccepted("M4", 10, "M5");

        assertEquals("M5", learner.getDecidedValue(),
                "Learner should decide once quorum is reached.");
        assertEquals("M5", CouncilMember.decidedValue,
                "Global decided value should be updated.");
        assertFalse(mockMember.getBroadcastedMessages().isEmpty(),
                "DECIDE message should be broadcast upon consensus.");
    }

    /**
     * Test that repeated ACCEPTED messages for the same candidate
     * do not change the decided value once consensus is reached.
     */
    @Test
    void testLearnerIgnoresExtraVotesAfterConsensus() {
        learner.handleAccepted("M2", 5, "M7");
        learner.handleAccepted("M3", 5, "M7");
        learner.handleAccepted("M4", 5, "M7"); // quorum reached

        // Try adding more votes
        learner.handleAccepted("M5", 5, "M7");
        learner.handleAccepted("M6", 5, "M7");

        assertEquals("M7", learner.getDecidedValue(),
                "Decided value should remain stable after consensus.");
        assertEquals(1, mockMember.getBroadcastedMessages().size(),
                "Only one DECIDE message should be broadcast.");
    }

    /**
     * Test that handleDecide() synchronizes decision correctly
     * when another member broadcasts the final DECIDE message.
     */
    @Test
    void testHandleDecideUpdatesValueWhenUnset() {
        learner.handleDecide("M2", 12, "M9");

        assertEquals("M9", learner.getDecidedValue(),
                "Learner should learn the decided value from another member.");
        assertEquals("M9", CouncilMember.decidedValue,
                "Shared global value should also reflect the decision.");
    }

    /**
     * Test that handleDecide() does not overwrite an already-decided value.
     */
    @Test
    void testHandleDecideDoesNotOverwriteExistingDecision() {
        // Already reached consensus on M4
        learner.handleAccepted("M2", 10, "M4");
        learner.handleAccepted("M3", 10, "M4");
        learner.handleAccepted("M4", 10, "M4");

        // Incoming decide for another candidate should not change it
        learner.handleDecide("M5", 11, "M8");

        assertEquals("M4", learner.getDecidedValue(),
                "Existing decided value should not be replaced by new DECIDE message.");
    }
}
