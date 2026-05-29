package paxos;

import java.util.Random;
// Simulates network behaviour for each council member.
// Introduces artificial delay or simulated node failures
// to mimic real-world latency and reliability conditions.
public class Profile {
    private static final Random random = new Random();

    //simulate artificial latency or failures depending on profile
    //this method can be called before sending or processing messages
    // profile paramter: the members network profile (reliable,latent, failure, standard)
    public static void simulateLatency(String profile) {
        try {
            switch (profile.toLowerCase()) {
                case "reliable":
                    //respond instantly no delay
                    //M1: very low latency(instant response)
                    Thread.sleep(50);
                    break;

                case "latent":
                    // M2:large unpredictable delays (eg: bad network)
                    //1-3 second delay to simulate bad connection
                    Thread.sleep(1000 + random.nextInt(2000));
                    break;

                case "failure":
                    // M3: occasionally simulate a crash or unresponsiveness
                    if (random.nextDouble() < 0.3) {  // 30% chance the node 'fails'
                        System.out.println("simulating failure: Node " + profile + " will become unresponsive");
                        // Freeze this thread indefinitely to mimic a dead node without crashing Maven
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        return; // stop executing further network logic
                    } else {
                        // normal operation delay when not failed
                        Thread.sleep(800 + random.nextInt(400));
                    }
                    break;
            }
        } catch (InterruptedException e) {
            //restore interrupted state(important for clean thread termination)
            Thread.currentThread().interrupt();
        }
    }

    //randomly decide whether this node should simulate crash or not
    // profile parameter the members network profile
    // return true if node should fail ; false otherwise.
    public static boolean shouldFail(String profile) {
        return profile.equalsIgnoreCase("failure") && random.nextDouble() < 0.3;
    }
}