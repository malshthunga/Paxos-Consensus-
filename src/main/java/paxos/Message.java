//---------------
//Message.java
//---------------
//this class defines the structure of messages exchanged between members
// in the paxos algorithm each message represents a communication event such as
// prepare, accept, learn
// messages are serialized and sent over sockets
// between members to reach distributed consensus.
//-----------------
package paxos;

import java.io.Serial;
import java.io.Serializable;
import com.google.gson.Gson;

// the message class represents one message that can be sent between nodes
// implements serializable so it can be transmitted over network streams
public class Message implements Serializable{
    //serialization
    @Serial
    private static final long serialVersionUID = 1L;

    private final String type;          // Message type (e.g., PREPARE, PROMISE, ACCEPT_REQUEST)
    private final String sender;        // ID of the sender (e.g., M1, M2, etc.)
    private final int proposalNum;      // Unique proposal/ballot number
    private final String candidate;     // Proposed or accepted candidate/value

    // CONSTRUCTORS -> create messages with different detail levels
    // constructor for basic message no balletID or value
    public Message(String type, String sender, int proposalNum, String candidate) {
        this.type = type;
        this.sender = sender;
        this.proposalNum = proposalNum;
        this.candidate = candidate;
    }

    //default no arg constructor (required for gson deserialization)
    public Message() {
        this.type = "";
        this.sender = "";
        this.proposalNum = -1;
        this.candidate = "";
    }

    // Getters (used by Proposer, Acceptor, Learner, etc.)
    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public int getProposalNum() {
        return proposalNum;
    }

    public String getCandidate() {
        return candidate;
    }

    // JSON Serialization and Deserialization (for sending messages as text)
    //Converts this Message object to a JSON string for network transmission.

    public String toJson() {
        return new Gson().toJson(this);
    }


     //Converts a JSON string back into a Message object.

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    // Readable text format (for console logging and debugging)

    @Override
    public String toString() {
        return String.format(
                "[TYPE:%s | SENDER:%s | PROPOSAL:%d | CANDIDATE:%s]",
                type, sender, proposalNum, candidate
        );
    }
}