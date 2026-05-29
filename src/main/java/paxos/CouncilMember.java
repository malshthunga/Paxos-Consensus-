package paxos;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;


//this class is a single peer in distributed council network
//each council member runs in its own thread and performs all three paxos roles
//1) proposer : initiates proposals for candidate
//2) acceptor: responds to prepare and accept requests
//3) learner : observers accepted messages and learns final consensus
// handles all TCP communication and delegates PAXOS logic to approporiate classes.


public class CouncilMember implements Runnable {
    //unique id for council member
    private final String id;
    //profile (reliable, latent, failure or standard)
    private final String profile;
    //used to receive incoming connections from other peers
    private ServerSocket server;
    // all members address
    private Map<String, InetSocketAddress> networkMap;
    //paxos role instances
    private final Proposer proposer;
    private final Acceptor acceptor;
    private final Learner learner;
    // Shared global variable for final consensus decision (visible to all members)
    // Declared volatile to ensure all threads see the latest value
    public static volatile String decidedValue = null;

    // CONSTRUCTOR
    // creates council member object and assigns ID and behaviour type
    public CouncilMember(String id, String profile, Map<String, InetSocketAddress> networkMap) {
        this.id = id;
        this.profile = profile;
        this.networkMap = networkMap;

        //initiate paxos role classes
        this.proposer = new Proposer(this);
        this.acceptor = new Acceptor(this);
        this.learner = new Learner(this);
    }

    // Each member must communicate using TCP/IP sockets and listen on a
    // unique port defined in 'network.config'.
    // This run() method implements that requirement:
    //  - Opens a ServerSocket for this member’s unique port
    //  - Listens for incoming TCP connections from other members
    //  - Reads text-based Paxos messages
    //  - Simulates network latency/failure according to the member’s profile
    //  - Passes each message to handleMessage() for Paxos logic processing
    //This is where the COMMUNICATION MECHANISM is implemented.
    @Override
    public void run() {
        try {
            //look up this port using the network configuration file
            int port = NetworkConfigReader.ports.get(id);

            //open the server socket for this member (each peer listens on a unique port)
            server = new ServerSocket(port);
            LoggerUtil.banner("Starting Council Member " + id);
            LoggerUtil.log(id , " started on port" + port + " (" + profile + ")");

            //MAIN LISTENER LOOP
            //infinite loop to handle incoming connections
            while (true) {
                Socket socket = server.accept(); //wait for someone to connect
                //Instead of manually sleeping, use Utils.safeSleep()
                Utils.safeSleep(50); // small artificial delay for realism
                //apply artificial delay depending on members profile
                //this is called before sending and processing messages in sendMessage() and run()
                //sendMessage() - before opening socket
                //run() - before processing incoming message
                Profile.simulateLatency(profile);
                if (Profile.shouldFail(profile)){
                    LoggerUtil.log(id, " simulating failure, shutting down...");
                    break; //simulate node crash
                }

                //read the incoming message
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {

                    String json = in.readLine(); // Read one line (the JSON message)
                    if (json != null && !json.isBlank()) {
                        // Deserialize the JSON string into a Message object
                        Message msg = Message.fromJson(json);

                        // Log the received message for debugging
                        LoggerUtil.log(id, "Received message: " + msg);

                        // Process the message based on its type
                        handleMessage(msg);
                    }
                } catch (Exception e) {
                    LoggerUtil.log(id, "Error reading message: " + e.getMessage());
                } finally {
                    // Always close the connection to avoid socket leaks
                    socket.close();
                }
            }

        } catch (IOException e) {
            LoggerUtil.log(id, "encountered an error: " + e.getMessage());

        }
    }
    // The sendMessage() method below implements the SENDING side of that requirement:
    //  - Establishes a TCP connection to the target member's unique port
    //  - Sends a text-based message (e.g., "PREPARE:M1:101:M5")
    //  - Simulates network delay or failure based on the member's profile
    // This method sends a message (in JSON format) to another member
    // by opening a TCP connection and writing the JSON string to its socket
    public void sendMessage(String targetId, String json) {
        InetSocketAddress address = networkMap.get(targetId);

        try {
            // Apply simulated delay or random failure (based on node profile)
            Profile.simulateLatency(profile);
            if (Profile.shouldFail(profile)) {
                LoggerUtil.log(id, "Failed to send message (simulated failure).");
                return;
            }

            // Connect to the recipient’s host and port, and send the message
            try (Socket socket = new Socket(address.getHostName(), address.getPort());
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println(json); // Send message as a single JSON line
                LoggerUtil.log(id, "Sent message to " + targetId + ": " + json);
            }

        } catch (IOException e) {
            LoggerUtil.log(id, "Could not contact " + targetId + " (" + e.getMessage() + ")");
        }
    }

    //broadcast message to all members except self
    //send JSON message to all other council members in the network
    public void broadcast(String json) {
        for (String memberId : networkMap.keySet()) {
            if (!memberId.equals(id)) {
                sendMessage(memberId, json);
            }
        }
    }

    // Handle incoming Paxos messages and delegate to correct role
    // Receives a parsed Message object and routes it
    // to the correct Paxos role (Proposer, Acceptor, or Learner)
    // based on message type.
    private void handleMessage(Message msg) {
        try {
            switch (msg.getType()) {
                case "PREPARE" -> // Phase 1A message
                        acceptor.handlePrepare(msg.getSender(), msg.getProposalNum(), msg.getCandidate());

                case "PROMISE" -> // Phase 1B message
                        proposer.handlePromise(msg);

                case "ACCEPT_REQUEST" -> // Phase 2A message
                        acceptor.handleAcceptRequest(msg.getSender(), msg.getProposalNum(), msg.getCandidate());

                case "ACCEPTED" -> // Phase 2B message
                        learner.handleAccepted(msg.getSender(), msg.getProposalNum(), msg.getCandidate());

                case "DECIDE" -> // Final consensus broadcast
                        learner.handleDecide(msg.getSender(), msg.getProposalNum(), msg.getCandidate());

                default -> LoggerUtil.log(id, "Unknown message type: " + msg.getType());
            }

        } catch (Exception e) {
            LoggerUtil.log(id, "Error processing message: " + e.getMessage());
        }
    }

    // Utility functions
    // Calculates quorum size (majority of total members)
    // For 9 members → quorum = 5
    public int quorumSize() {
        return (networkMap.size() / 2) + 1;
    }

    // Returns this node's unique ID (e.g., "M4")
    public String getId() {
        return id;
    }

    // Returns the map of all network addresses
    public Map<String, InetSocketAddress> getNetworkMap() {
        return networkMap;
    }

    // Provides access to this node’s Proposer instance
    public Proposer getProposer() {
        return proposer;
    }

    // Allows Main.java to start a proposal easily
    public void initiateProposal(String candidate) {
        this.setPendingCandidate(candidate);
        proposer.initiateProposal(candidate);
    }
    // Stores all PROMISE-related data per proposal number
    private final Map<Integer, Message> promiseData = new ConcurrentHashMap<>();

    // Stores the proposer's own initial candidate (e.g., M5)
    private String pendingCandidate;

    // Called by proposer when initiating a new proposal
    public void setPendingCandidate(String candidate) {
        this.pendingCandidate = candidate;
    }

    // Retrieve the proposer's original candidate
    public String getPendingCandidate() {
        return pendingCandidate;
    }

    // Records acceptor data when PROMISE messages are received
    // Only replaces stored data if this acceptor's promise refers to
    // a higher previously accepted proposal number.
    public void recordPromiseData(int proposalNum, Message msg) {
        Message current = promiseData.get(proposalNum);
        // Only store if acceptor had previously accepted a value
        if (msg.getProposalNum() >= 0) {
            if (current == null || msg.getProposalNum() > current.getProposalNum()) {
                promiseData.put(proposalNum, msg);
            }
        }
    }

    // Retrieves the highest accepted value for a given proposal (if any)
    public String getHighestAcceptedValue(int proposalNum) {
        Message msg = promiseData.get(proposalNum);
        return (msg != null) ? msg.getCandidate() : null;
    }
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: CouncilMember <MemberID> <Profile>");
            System.exit(1);
        }

        String id = args[0];
        String profile = args[1];
        Map<String, InetSocketAddress> networkMap =
                NetworkConfigReader.loadNetworkConfig("network.config");

        CouncilMember member = new CouncilMember(id, profile, networkMap);
        new Thread(member, id).start();

        System.out.println("Council member " + id + " is now active with profile " + profile);
    }

}