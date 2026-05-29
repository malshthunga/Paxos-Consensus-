package paxos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


// Handles Paxos Learner responsibilities for a single CouncilMember.
// Each Learner:
//  1. Listens for ACCEPTED messages from Acceptors.
//  2. Tracks how many Acceptors have accepted each candidate.
//  3. Declares consensus once a majority accepts the same value,
//     and broadcasts a DECIDE message so all peers learn it.
// Works seamlessly with Proposer.java, Acceptor.java, and CouncilMember.java.
// Uses LoggerUtil for logging and Message.java (JSON-based communication).


public class Learner {

    // Reference to parent CouncilMember (for broadcasting messages)
    private final CouncilMember member;

    // Tracks how many members accepted each candidate
    private final Map<String, Set<String>> acceptedVotes = new ConcurrentHashMap<>();

    // Once consensus is reached, store the final decision
    private String decidedValue = "";

    // Constructor
    public Learner(CouncilMember member) {
        this.member = member;
    }

    // METHOD: handleAccepted()
    // Called when a Learner receives an ACCEPTED message.
    // Tracks votes and declares consensus once a quorum is reached.

    public void handleAccepted(String senderId, int proposalNum, String candidate) {
        acceptedVotes.computeIfAbsent(candidate, k -> new HashSet<>()).add(senderId);

        int count = acceptedVotes.get(candidate).size();
        LoggerUtil.log(member.getId(),
                "Learner registered ACCEPTED from " + senderId +
                        " for candidate " + candidate + " (" + count + " votes)");

        // If a majority of members have accepted this value → consensus!
        if (count >= member.quorumSize() && decidedValue.isEmpty()) {
            decidedValue = candidate;

            // Update the global shared decision so Main.java can access it
            if (CouncilMember.decidedValue == null) {
                CouncilMember.decidedValue = candidate;
            }

            LoggerUtil.alert(member.getId(),
                    "CONSENSUS REACHED → " + candidate + " has been elected Council President!");

            // Broadcast DECIDE message to all other members
            broadcastDecision(proposalNum, candidate);
        }
    }

    // METHOD: handleDecide()
    // Called when another member broadcasts a DECIDE message.
    // Ensures all members agree on the final value.

    public void handleDecide(String senderId, int proposalNum, String candidate) {
        if (decidedValue.isEmpty()) {
            decidedValue = candidate;

            if (CouncilMember.decidedValue == null) {
                CouncilMember.decidedValue = candidate;
            }

            LoggerUtil.log(member.getId(),
                    "Learned final consensus from " + senderId + ": " + candidate);
        }
    }


    // METHOD: broadcastDecision()
    // Sends a DECIDE message (as JSON) to all other members once consensus is reached.

    private void broadcastDecision(int proposalNum, String candidate) {
        Message decideMsg = new Message(
                "DECIDE",
                member.getId(),
                proposalNum,
                candidate
        );

        // Broadcast JSON version to all nodes
        member.broadcast(decideMsg.toJson());
    }

    // Getter for decided value

    public String getDecidedValue() {
        return decidedValue;
    }
}
