package paxos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CouncilMemberTest.java
 * ----------------------
 * Focused unit tests for logical methods inside CouncilMember.
 * Tests exclude socket communication and concurrency,
 * verifying only quorum size, candidate management, and promise storage logic.
 *
 * Author: Nethmi Malsha Ranathunga
 * Course: Distributed Systems - Assignment 3
 */
public class CouncilMemberTest {

    private CouncilMember member;
    private Map<String, InetSocketAddress> mockNetwork;

    @BeforeEach
    void setUp() {
        // Create dummy network with 9 members
        mockNetwork = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            mockNetwork.put("M" + i, new InetSocketAddress("localhost", 9000 + i));
        }

        member = new CouncilMember("M1", "reliable", mockNetwork);
    }

    @Test
    void testQuorumSizeCalculation() {
        int quorum = member.quorumSize();
        assertEquals(5, quorum, "For 9 members, quorum should be 5 (majority rule).");
    }

    @Test
    void testSetAndGetPendingCandidate() {
        member.setPendingCandidate("M5");
        assertEquals("M5", member.getPendingCandidate(), "Pending candidate should match the one set.");
    }

    @Test
    void testRecordPromiseDataStoresHigherProposal() {
        Message lower = new Message("PROMISE", "M2", 10, "M5");
        Message higher = new Message("PROMISE", "M2", 15, "M8");

        member.recordPromiseData(1, lower);
        member.recordPromiseData(1, higher);

        String highest = member.getHighestAcceptedValue(1);
        assertEquals("M8", highest, "Should store candidate from highest proposal number.");
    }

    @Test
    void testRecordPromiseDataDoesNotReplaceWithLowerProposal() {
        Message higher = new Message("PROMISE", "M3", 20, "M7");
        Message lower = new Message("PROMISE", "M3", 10, "M4");

        member.recordPromiseData(2, higher);
        member.recordPromiseData(2, lower);

        String highest = member.getHighestAcceptedValue(2);
        assertEquals("M7", highest, "Lower proposal should not overwrite higher proposal data.");
    }

    @Test
    void testGetHighestAcceptedValueReturnsNullIfNoPromise() {
        assertNull(member.getHighestAcceptedValue(99),
                "Should return null if no PROMISE data exists for given proposal number.");
    }
}
