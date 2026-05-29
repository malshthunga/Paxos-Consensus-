package paxos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AcceptorTest.java
 * -----------------
 * Unit tests for the Acceptor class in the Paxos Council project.
 * These tests focus on verifying Paxos protocol rules for promises and acceptances.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class AcceptorTest {

    private Acceptor acceptor;
    private MockCouncilMember mockMember;
    private List<String> sentMessages;

    /**
     * Simple mock version of CouncilMember to intercept sendMessage() calls
     * and store messages instead of actually sending them over the network.
     */
    static class MockCouncilMember extends CouncilMember {
        private final List<String> sentMessages = new ArrayList<>();
        private String pendingCandidate = "M5";

        public MockCouncilMember(String id) {
            super(id, "reliable", new HashMap<>());
        }

        @Override
        public void sendMessage(String targetId, String json) {
            sentMessages.add(json);
        }

        @Override
        public String getPendingCandidate() {
            return pendingCandidate;
        }

        public List<String> getSentMessages() {
            return sentMessages;
        }
    }

    @BeforeEach
    void setUp() {
        mockMember = new MockCouncilMember("M1");
        acceptor = new Acceptor(mockMember);
        sentMessages = mockMember.getSentMessages();
    }

    /**
     * Test that handlePrepare() sends a PROMISE message when the proposal
     * number is higher than any previous promise.
     */
    @Test
    void testHandlePrepareSendsPromiseOnNewProposal() {
        acceptor.handlePrepare("M2", 10, "M7");

        // Verify PROMISE was sent
        assertFalse(sentMessages.isEmpty(), "Acceptor should send a PROMISE message");
        Message promise = Message.fromJson(sentMessages.get(0));

        assertEquals("PROMISE", promise.getType());
        assertEquals("M1", promise.getSender(), "Sender should be this acceptor");
        assertEquals(10, promise.getProposalNum());
        assertEquals("M7", promise.getCandidate());
        assertEquals(10, acceptor.getPromisedNum(), "promisedNum should be updated");
    }

    /**
     * Test that handlePrepare() ignores lower proposal numbers.
     */
    @Test
    void testHandlePrepareIgnoresLowerProposal() {
        acceptor.handlePrepare("M2", 15, "M8");
        int previousPromised = acceptor.getPromisedNum();
        sentMessages.clear();

        // Send a lower-numbered proposal
        acceptor.handlePrepare("M3", 10, "M9");

        assertTrue(sentMessages.isEmpty(), "Lower proposal should not trigger PROMISE");
        assertEquals(previousPromised, acceptor.getPromisedNum(), "promisedNum should remain unchanged");
    }

    /**
     * Test that handleAcceptRequest() accepts a proposal when n >= promisedNum.
     */
    @Test
    void testHandleAcceptRequestAcceptsValidProposal() {
        acceptor.handlePrepare("M2", 10, "M6");  // promise first
        sentMessages.clear();

        acceptor.handleAcceptRequest("M2", 10, "M6");

        assertFalse(sentMessages.isEmpty(), "Should send ACCEPTED message");
        Message accepted = Message.fromJson(sentMessages.get(0));

        assertEquals("ACCEPTED", accepted.getType());
        assertEquals(10, acceptor.getAcceptedNum());
        assertEquals("M6", acceptor.getAcceptedValue());
    }

    /**
     * Test that handleAcceptRequest() rejects when proposal < promisedNum.
     */
    @Test
    void testHandleAcceptRequestRejectsLowerProposal() {
        // First accept a higher proposal
        acceptor.handlePrepare("M2", 20, "M7");
        sentMessages.clear();

        // Now attempt lower proposal
        acceptor.handleAcceptRequest("M3", 10, "M9");

        assertTrue(sentMessages.isEmpty(), "Should not send ACCEPTED for lower proposal");
        assertNotEquals(10, acceptor.getAcceptedNum(), "AcceptedNum should not be updated");
    }

    /**
     * Test that the fallback to member.getPendingCandidate() works
     * when candidate is missing and no previous value exists.
     */
    @Test
    void testHandlePrepareFallbackToPendingCandidate() {
        acceptor.handlePrepare("M2", 30, "");  // Empty candidate → should fallback

        assertFalse(sentMessages.isEmpty(), "PROMISE should still be sent");
        Message promise = Message.fromJson(sentMessages.get(0));

        assertEquals("PROMISE", promise.getType());
        assertEquals("M5", promise.getCandidate(), "Should fallback to pending candidate from member");
    }
}
