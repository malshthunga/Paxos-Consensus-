package paxos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ProposerTest.java
 * -----------------
 * Unit tests for the Proposer class in the Paxos Council project.
 * These tests verify correct proposal number generation, promise handling,
 * quorum detection, and message broadcast behavior.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class ProposerTest {

    private Proposer proposer;
    private MockCouncilMember mockMember;

    /**
     * Mock version of CouncilMember used for unit testing.
     * This isolates the Proposer logic and avoids real socket communication.
     */
    static class MockCouncilMember extends CouncilMember {
        private final List<String> sentMessages = new ArrayList<>();
        private String pendingCandidate;
        private final Map<Integer, String> acceptedValues = new HashMap<>();

        public MockCouncilMember(String id) {
            // Minimal constructor replacement for testing
            super(id, "reliable", new HashMap<>());

        }

        @Override
        public void broadcast(String jsonMessage) {
            sentMessages.add(jsonMessage);
        }

        @Override
        public int quorumSize() {
            return 3; // simple majority for testing
        }

        @Override
        public void setPendingCandidate(String candidate) {
            this.pendingCandidate = candidate;
        }

        @Override
        public String getPendingCandidate() {
            return pendingCandidate;
        }

        @Override
        public void recordPromiseData(int proposalNum, Message msg) {
            acceptedValues.put(proposalNum, msg.getCandidate());
        }

        @Override
        public String getHighestAcceptedValue(int proposalNum) {
            return acceptedValues.get(proposalNum);
        }

        public List<String> getSentMessages() {
            return sentMessages;
        }
    }

    @BeforeEach
    void setUp() {
        mockMember = new MockCouncilMember("M1");
        proposer = new Proposer(mockMember);
    }

    /**
     * Ensures that proposal numbers are unique and incorporate member ID.
     * Proposal number formula: (counter * 10) + numericID
     */
    @Test
    void testGenerateUniqueProposalNumbers() throws Exception {
        // Using reflection since generateProposalNum() is private
        var method = Proposer.class.getDeclaredMethod("generateProposalNum");
        method.setAccessible(true);

        int p1 = (int) method.invoke(proposer);
        int p2 = (int) method.invoke(proposer);

        assertNotEquals(p1, p2, "Each proposal number must be unique");
        assertTrue(p1 % 10 == 1, "Proposal number should include member ID (M1 → ends with 1)");
    }

    /**
     * Tests that initiating a proposal sends a PREPARE message and stores the candidate.
     */
    @Test
    void testInitiateProposalSendsPrepare() {
        proposer.initiateProposal("M5");

        List<String> messages = mockMember.getSentMessages();
        assertEquals(1, messages.size(), "One message should be broadcast during proposal initiation");

        Message sent = Message.fromJson(messages.get(0));
        assertEquals("PREPARE", sent.getType());
        assertEquals("M1", sent.getSender());
        assertEquals("M5", sent.getCandidate());
    }

    /**
     * Tests that handlePromise correctly tracks PROMISE messages and triggers
     * an ACCEPT_REQUEST broadcast once quorum is reached.
     */
    @Test
    void testHandlePromiseTriggersAcceptRequestOnQuorum() {
        // Create 3 PROMISE messages to reach quorum
        Message promise1 = new Message("PROMISE", "M2", 10, "M5");
        Message promise2 = new Message("PROMISE", "M3", 10, "M5");
        Message promise3 = new Message("PROMISE", "M4", 10, "M5");

        proposer.handlePromise(promise1);
        proposer.handlePromise(promise2);
        proposer.handlePromise(promise3);

        // Verify an ACCEPT_REQUEST message was sent after quorum
        boolean foundAcceptReq = mockMember.getSentMessages().stream()
                .map(Message::fromJson)
                .anyMatch(m -> m.getType().equals("ACCEPT_REQUEST"));

        assertTrue(foundAcceptReq, "Proposer should broadcast ACCEPT_REQUEST once quorum is reached");
    }
}
