package paxos;

import java.text.SimpleDateFormat;
import java.util.Date;


// Simple utility class for consistent, timestamped console logging
// across all Paxos components (CouncilMember, Proposer, Acceptor).

public class LoggerUtil {

    // Date formatter for log timestamps
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");


//     Logs a formatted message to the console with timestamp and node ID.
//     @param memberId the ID of the member producing the log (e.g., "M1")
//     @param message the message content to display

    public static void log(String memberId, String message) {
        String timestamp = dateFormat.format(new Date());
        System.out.printf("[%s] [%s] %s%n", timestamp, memberId, message);
    }


//     Prints a clean banner header — useful for marking scenarios or test sections.
//     @param title header message

    public static void banner(String title) {
        String line = "=".repeat(Math.max(20, title.length() + 5));
        System.out.println("\n" + line);
        System.out.println(" " + title.toUpperCase());
        System.out.println(line + "\n");
    }


//     Logs important events or alerts (with visible ⚠ markers).
//     @param memberId the ID of the member
//     @param message alert message

    public static void alert(String memberId, String message) {
        String timestamp = dateFormat.format(new Date());
        System.out.printf("[%s] [%s] ⚠ %s ⚠%n", timestamp, memberId, message);
    }
}
