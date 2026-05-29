package paxos;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;


// General utility class for Paxos.
// Handles random candidate selection, string formatting,
// safe thread sleeping, converting message types, and timing utilities for tests.

public class Utils {

    private static final Random random = new Random();


//     Safely pauses the thread for a given duration in milliseconds.
//     Handles InterruptedException properly.

    public static void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


//     Selects a random candidate from the given list of member IDs.
//     Ensures proposer doesn't vote for itself unless necessary.
//
//     @param allIds list of all member IDs
//     @param selfId ID of the current member
//     @return randomly selected candidate ID

    public static String randomCandidate(List<String> allIds, String selfId) {
        List<String> copy = new ArrayList<>(allIds);
        copy.remove(selfId);
        if (copy.isEmpty()) return selfId; // fallback
        return copy.get(random.nextInt(copy.size()));
    }


//     Simple helper to check if a string is null or empty.
//     @param s the string to check
//     @return true if null or empty

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }


//     Returns a formatted divider line for console output.
//     @param label label text for divider
//     @return formatted string divider

    public static String divider(String label) {
        String line = "=".repeat(Math.max(15, label.length() + 5));
        return String.format("\n%s\n  %s\n%s\n", line, label.toUpperCase(), line);
    }


//     Converts message type string into standard uppercase keyword.
//     @param type message type string
//     @return uppercase version

    public static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }


//     Returns the current system time formatted as HH:mm:ss.SSS.
//     Useful for manual timing or quick printouts.
//     @return formatted timestamp

    public static String now() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }
}
