package paxos;

import java.util.*;


// Handles all Paxos acceptor responsibilities for a single council member.
// Each CouncilMember object owns an Acceptor instance.
// The Acceptor stores and responds to proposals while following
// Paxos protocol safety rules (Phase 1B and 2B).

// Works with CouncilMember.java, Proposer.java, and Learner.java.
// Uses LoggerUtil for consistent, timestamped logs.
// Uses Message.java (JSON-based) for structured network communication.


public class Acceptor {

    // Reference to the parent CouncilMember (for sending messages)
    private final CouncilMember member;

    // Local Paxos state
    private int promisedNum = -1;      // Highest proposal number promised
    private int acceptedNum = -1;      // Highest proposal number accepted
    private String acceptedValue = ""; // Value accepted with acceptedNum

    // Constructor
    public Acceptor(CouncilMember member) {
        this.member = member;
    }

    // PHASE 1B: handlePrepare()
    // Responds to a PREPARE message from a proposer.
    // If proposal number n > any previous promise, the Acceptor promises
    // not to accept lower proposals and replies with PROMISE(n, accepted_v).
    // Otherwise, it ignores the message.

    public void handlePrepare(String senderId, int proposalNum, String candidate) {
        LoggerUtil.log(member.getId(),
                "Received PREPARE from " + senderId +
                        " [n=" + proposalNum + ", v=" + candidate + "]");

        if (proposalNum > promisedNum) {
            promisedNum = proposalNum;

            // Determine what value to include in the PROMISE
            String candidateToSend;
            if (acceptedValue != null && !acceptedValue.isEmpty()) {
                // If this acceptor already accepted something before, share it
                candidateToSend = acceptedValue;
            } else if (candidate != null && !candidate.isEmpty()) {
                // Otherwise, use the candidate proposed by the sender
                candidateToSend = candidate;
            } else {
                // As a fallback, use this member's pending candidate (from its own proposer)
                candidateToSend = member.getPendingCandidate();
            }

            LoggerUtil.log(member.getId(),
                    "Promising proposal n=" + proposalNum +
                            " (prevAccepted=" + acceptedNum + ", value=" + candidateToSend + ")");

            // Send PROMISE including the valid candidate
            Message promise = new Message(
                    "PROMISE",
                    member.getId(),
                    proposalNum,
                    candidateToSend
            );

            member.sendMessage(senderId, promise.toJson());
        } else {
            LoggerUtil.log(member.getId(),
                    "Ignored PREPARE from " + senderId +
                            " (proposal " + proposalNum + " < promised " + promisedNum + ")");
        }
    }



    // PHASE 2A/2B: handleAcceptRequest()
    // Responds to an ACCEPT_REQUEST message from a proposer.
    // If proposal n >= promisedNum, accepts (n, v) and replies ACCEPTED(n, v).
    // Otherwise, rejects (since it already promised a higher proposal).

    public void handleAcceptRequest(String senderId, int proposalNum, String candidate) {
        LoggerUtil.log(member.getId(),
                "Received ACCEPT_REQUEST from " + senderId +
                        " [n=" + proposalNum + ", v=" + candidate + "]");

        if (proposalNum >= promisedNum) {
            promisedNum = proposalNum;
            acceptedNum = proposalNum;
            acceptedValue = candidate;

            LoggerUtil.log(member.getId(),
                    "Accepted value '" + candidate + "' with proposal n=" + proposalNum);

            // Build ACCEPTED message
            Message acceptedMsg = new Message(
                    "ACCEPTED",
                    member.getId(),
                    proposalNum,
                    candidate
            );

            member.sendMessage(senderId, acceptedMsg.toJson());
        } else {
            LoggerUtil.log(member.getId(),
                    "Rejected ACCEPT_REQUEST from " + senderId +
                            " (proposal " + proposalNum + " < promised " + promisedNum + ")");
        }
    }

    // Optional Accessors (for debugging or testing)

    public int getPromisedNum() {
        return promisedNum;
    }

    public int getAcceptedNum() {
        return acceptedNum;
    }

    public String getAcceptedValue() {
        return acceptedValue;
    }
}
