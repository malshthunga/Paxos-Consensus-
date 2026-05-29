package paxos;

import java.io.*;
import java.util.*;
import java.net.*;
// Implements the configuration reader for the Paxos communication setup.
// This class loads the `network.config` file, which defines each council
// member’s hostname and port number. It returns a map linking member IDs
// (e.g., "M1") to their corresponding InetSocketAddress so all members
// can connect with each other via TCP sockets.

//Reads and stores the network configuration for the Paxos council members.
// Each line of the `network.config` file must have the format:
//eg: M1, localhost, 9001
//lines starting with # are treated as comments and ignored.

public class NetworkConfigReader {
    //global static map so other council member classes can use it
    public static final Map<String, Integer> ports = new HashMap<>();
    //load the network config from txt file
    // filename: name of configuration file(network.config)
    //return a map linking each memberID to its InetSocketAddress
    //throw IOException if theres an error reading the configuration file
    public static Map<String, InetSocketAddress> loadNetworkConfig(String filename) throws IOException {
        Map<String, InetSocketAddress> networkMap = new HashMap<>();

        //try with resources ensures file is closed automatically
        try ( BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //each line example : M1, localhost, 9001
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // skip comments
                }
                //expected format: M1, localhost, 9001
                String[] parts = line.trim().split(",");
                if (parts.length == 3) {
                    String member = parts[0].trim();
                    String host = parts[1].trim();
                    int port = Integer.parseInt(parts[2].trim());
                    //Add entry to both maps
                    networkMap.put(member, new InetSocketAddress(host, port));
                    ports.put(member, port);
                } else {
                    System.err.println("Invalid config line: " + line);
                }
            }
        }
        System.out.println("Loaded network configuration for " +  networkMap.size() + " members.");
        return networkMap;
    }
}