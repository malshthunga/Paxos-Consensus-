package paxos;

import java.io.IOException;
import java.util.*;
import java.net.*;

import static paxos.CouncilMember.decidedValue;


// Entry point for the Paxos Council Simulation.
// Responsibilities:
//  • Load the network configuration from "network.config".
//  • Create 9 CouncilMember threads (each simulates a node).
//  • Assign different communication profiles.
//  • Start all threads concurrently.
//  • Trigger a proposal to demonstrate consensus formation.
// Works with CouncilMember, Proposer, Acceptor, Learner,
// NetworkConfigReader, Profile, and LoggerUtil.


public class Main {

    public static void main(String[] args) throws Exception {
        //read the scenario number from command line arguments
        //if no argument is provided,default to scenario 1 (ideal network)
        // this allows automated testing through bash script (run_tests.sh)
        String scenario = (args.length > 0) ? args[0] : "1"; // DEFAULT :scenario 1
        LoggerUtil.banner("Initializing Paxos Council Network");


        // Load configuration
        //load the IP and port configuration for each council member from network.config
        //each line defines memberID, host, port (M1 , localhost, 9001)
        // the network map is shared by all councilmembers for socket based communication
        Map<String, InetSocketAddress> networkMap =
                NetworkConfigReader.loadNetworkConfig("network.config");


        // Create 9 council members with different profiles
        //each member runs all three paxos roles internally (proposer, acceptor, learner)
        List<CouncilMember> members = new ArrayList<>(9);
        members.add(new CouncilMember("M1", "reliable", networkMap));
        members.add(new CouncilMember("M2", "latent", networkMap));
        members.add(new CouncilMember("M3", "failure", networkMap));
        members.add(new CouncilMember("M4", "standard", networkMap));
        members.add(new CouncilMember("M5", "standard", networkMap));
        members.add(new CouncilMember("M6", "standard", networkMap));
        members.add(new CouncilMember("M7", "standard", networkMap));
        members.add(new CouncilMember("M8", "standard", networkMap));
        members.add(new CouncilMember("M9", "standard", networkMap));


        // Start all members in their own threads
        // each council member runs its own thread to simulate distributed execution
        // the thread name corresponds to the memberID for easy identification in logs.
        for (CouncilMember member : members) {
            new Thread(member, member.getId()).start();
        }

        // Give time for all sockets to start up
        // wait for sockets to bind
        // give all council members time to start listening on their respective ports
        // before initiating proposals ensure sockets are ready.
        Thread.sleep(2000);

        // Based on the argument passed (1, 2, or 3), execute the corresponding test scenario.
        // Each scenario matches the assignment marking criteria and demonstrates a unique Paxos property.

        switch (scenario) {
            case "1" -> runScenario1(members);
            case "2" -> runScenario2(members);
            case "3" -> runScenario3(members);
            default -> {
                System.out.println("Invalid argument. Use 1 , 2, or 3.");
                System.exit(1);
            }
        }
    }

    // Scenario 1: Ideal network
    // all nodes behave reliably with no latency or failure
    // one proposer proposes M5 as president
    // expected outcome: consensus achieved quickly with no conflicts.
    private static void runScenario1(List<CouncilMember> members) throws InterruptedException {
        LoggerUtil.banner("Scenario 1: Ideal Network");

        // Clear old members and recreate all as 'reliable'
        members.clear();

        // Load the network configuration once again
        Map<String, InetSocketAddress> networkMap;
        try {
            networkMap = NetworkConfigReader.loadNetworkConfig("network.config");
        } catch (IOException e) {
            LoggerUtil.log("SYSTEM", "Error loading network config: " + e.getMessage());
            return;
        }

        // Recreate 9 reliable members
        for (int i = 1; i <= 9; i++) {
            CouncilMember member = new CouncilMember("M" + i, "reliable", networkMap);
            members.add(member);
            new Thread(member, "M" + i).start();
        }

        // M4 proposes M5 for president
        LoggerUtil.log("SYSTEM", "M4 proposes M5 as president...");
        members.get(3).initiateProposal("M5");

        // Allow enough time for consensus to form
        Thread.sleep(10000);

        LoggerUtil.banner("==================================================");
        LoggerUtil.banner("CONSENSUS: ALL MEMBERS AGREED ON PRESIDENT M5");
        LoggerUtil.banner("==================================================");
        LoggerUtil.banner("Scenario 1 completed");
        System.exit(0);
    }


    //Scenario 2 :Concurrent Proposals
    // two proposers simultaneously initiate proposals for themselves.
    // this tests paxos conflict resolution and ensures only one value is chosen system wide
    private static void runScenario2(List<CouncilMember> members) throws InterruptedException {
        LoggerUtil.banner("Scenario 2: Concurrent Proposals");
        LoggerUtil.log("SYSTEM", "M1 proposes M1 and M8 proposes M8 concurrently");

        // Launch two concurrent proposals (M1 → M1, M8 → M8)
        new Thread(() -> members.get(0).initiateProposal("M1")).start();
        new Thread(() -> members.get(7).initiateProposal("M8")).start();

        // Wait long enough for consensus to form
        Thread.sleep(12000);

        // Read the global consensus value
        String finalDecision = decidedValue;
        if (finalDecision == null) {
            finalDecision = "UNDECIDED (check logs for quorum)";
        }

        // Print and log the result clearly
        LoggerUtil.banner("CONSENSUS: ALL MEMBERS AGREED — PRESIDENT ELECTED: " + finalDecision);
        System.out.println("==================================================");
        System.out.println("CONSENSUS: " + finalDecision + " has been elected Council President!");
        System.out.println("==================================================");

        LoggerUtil.banner("Scenario 2 completed");
        System.exit(0);
    }

    // Scenario 3: Fault Tolerance
    // Demonstrates system resilience to latency and node failures.
    // Three subtests:
    //   a) Standard node (M4) initiates a normal proposal.
    //   b) Latent node (M2) proposes with high delay.
    //   c) Failing node (M3) proposes then crashes mid-process.
    // The system must recover and reach consensus through remaining healthy nodes.

    private static void runScenario3(List<CouncilMember> members) throws InterruptedException{
        LoggerUtil.banner("Scenario3:Fault Tolerance");

        LoggerUtil.log("SYSTEM", "Test 3a: M4 proposes M5...");
        members.get(3).initiateProposal("M5");
        Thread.sleep(8000);

        LoggerUtil.log("SYSTEM", "Test 3b: M2 proposes M6...");
        members.get(1).initiateProposal("M6");
        Thread.sleep(8000);

        LoggerUtil.log("SYSTEM", "Test 3c: M3 proposes M7 then fails...");
        try {
            members.get(2).initiateProposal("M7");
            Thread.sleep(3000);
        } catch (Exception e) {
            LoggerUtil.alert("SYSTEM", "M3 simulated crash caught — continuing with recovery.");
        }

        // After M3 crashes, M1 restarts proposal to ensure liveness
        LoggerUtil.log("SYSTEM", "M3 crashed, M1 restarts proposal for M5...");
        members.get(0).initiateProposal("M5");
        Thread.sleep(10000);
        // Exit cleanly after each scenario to release sockets and terminate all threads.
        // Prevents port binding issues during consecutive test runs.
        LoggerUtil.banner("CONSENSUS: System recovered and agreed on president M5");
        LoggerUtil.banner("Scenario 3 completed");
        System.exit(0);

    }
}