package paxos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Handles all Paxos proposer responsibilities for a single council member.
// Each CouncilMember object in the system owns a Proposer instance.

// Responsibilities:
//  1. Send PREPARE requests (Phase 1A)
//  2. Collect PROMISE replies (Phase 1B)
//  3. Once a quorum is reached, send ACCEPT_REQUEST messages (Phase 2A)

// Works together with CouncilMember.java, Acceptor.java, and Learner.java
// Uses LoggerUtil for logging and Gson-based Message serialization.


public class Proposer {

    // Reference to the CouncilMember that owns this proposer
    private final CouncilMember member;

    // Keeps track of members who sent PROMISE messages for each proposal number
    private final Map<Integer, Set<String>> promiseTracker = new ConcurrentHashMap<>();

    // Random is used only to pick a random candidate if none has been previously accepted
    private final Random random = new Random();

    // Shared counter for generating unique proposal numbers
    private static final java.util.concurrent.atomic.AtomicInteger counter =
            new java.util.concurrent.atomic.AtomicInteger(0);

    // Constructor: links proposer with its parent CouncilMember
    public Proposer(CouncilMember member) {
        this.member = member;
    }

    // Phase 1A: Initiate Proposal

    // Generates a unique proposal number and broadcasts a PREPARE message
    public void initiateProposal(String candidate) {
        int proposalNum = generateProposalNum();

        LoggerUtil.log(member.getId(),
                "Initiating proposal for " + candidate + " [proposal " + proposalNum + "]");

        member.setPendingCandidate(candidate);
        // Create PREPARE message and broadcast to all members
        Message prepare = new Message("PREPARE", member.getId(), proposalNum, candidate);
        member.broadcast(prepare.toJson());  // Convert to JSON before sending
    }

    // Phase 1B: Handle PROMISE

    // Processes a PROMISE message received from acceptors
    public void handlePromise(Message msg) {
        int proposalNum = msg.getProposalNum();
        String sender = msg.getSender();

        // Record the PROMISE sender for this proposal
        promiseTracker.computeIfAbsent(proposalNum, k -> new HashSet<>()).add(sender);
        int count = promiseTracker.get(proposalNum).size();

        LoggerUtil.log(member.getId(),
                "Received PROMISE from " + sender + " for proposal " + proposalNum +
                        " (" + count + " / " + member.quorumSize() + ")");

        // Track the highest numbered accepted proposal
        member.recordPromiseData(proposalNum, msg);

        // Once a quorum is reached, move to Phase 2A
        if (count >= member.quorumSize()) {
            String valueToPropose = member.getHighestAcceptedValue(proposalNum);

            // Robust fallback if the candidate is missing or empty
            if (valueToPropose == null || valueToPropose.isEmpty()) {
                LoggerUtil.log(member.getId(),
                        " No accepted value found — reverting to pending candidate.");
                valueToPropose = member.getPendingCandidate();
            }

            //  Just in case it's still empty (extra safety)
            if (valueToPropose == null || valueToPropose.isEmpty()) {
                LoggerUtil.log(member.getId(),
                        " Candidate still missing — setting to default 'UNKNOWN'.");
                valueToPropose = "UNKNOWN";
            }

            LoggerUtil.log(member.getId(),
                    "Quorum reached → sending ACCEPT_REQUEST for " + valueToPropose);

            Message acceptReq = new Message("ACCEPT_REQUEST", member.getId(), proposalNum, valueToPropose);
            member.broadcast(acceptReq.toJson());
        }
    }


    // Helper: Generate Unique Proposal Number
    private int generateProposalNum() {
        // Proposal number = (counter * 10) + numeric ID of the member
        return counter.incrementAndGet() * 10 + Integer.parseInt(member.getId().substring(1));
    }

    // Helper: Select Random Candidate

    private String selectCandidate() {
        List<String> allIds = new ArrayList<>(member.getNetworkMap().keySet());
        allIds.remove(member.getId()); // Don’t vote for yourself unless needed
        return allIds.get(random.nextInt(allIds.size()));
    }
}
