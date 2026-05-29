package paxos;

//This defines all valid Paxos message types exchanged between council members
//keeps message names consistent across system and prevents typos when comparing strings.

//each message type corresponds to specific phase of paxos algorithm
//PREPARE -> PHASE 1A : proposer requests promises
//PROMISE -> PHASE 1B: Acceptor responds with a promise
// ACCEPT REQUEST -> PHASE 2A: Proposer requests value acceptance
// ACCEPTED -> PHASE 2B: Acceptor confirms value acceptance
// DECIDE -> PHASE 3: Learners broadcast the final chosen value

public enum MessageType {
    PREPARE, //PROPOSER -> all acceptors (PHASE 1A)
    PROMISE, // ACCEPTOR -> Proposer (PHASE 1B)
    ACCEPT_REQUEST, // PROPOSER-> Acceptors(PHASE 2A)
    ACCEPTED, // ACCEPTOR-> ALL (PHASE 2B)
    DECIDE,// LEARNER -> ALL (Consensus achieved)

    //extra for debugging
    REJECT, // sent when proposal is rejected
    LEARN, // used for learner synchronization
    HEARTBEAT // used for health checks or test messages
}
