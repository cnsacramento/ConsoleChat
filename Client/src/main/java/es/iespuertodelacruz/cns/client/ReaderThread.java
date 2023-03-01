package es.iespuertodelacruz.cns.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public  class ReaderThread extends Thread {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private Client client;
    private long endPingTime;

    public ReaderThread(BufferedReader reader, PrintWriter writer, Client client) {
        this.reader = reader;
        this.writer = writer;
        this.client = client;
    }

    public void run() {
        try {
            String inputServer;
            while ((inputServer = reader.readLine()) != null) {
                if(inputServer.startsWith("/ping")) {
                    String[] params = inputServer.split(" ");
                    String pingSender = params[1];
                    String uuid = params[2];
                    writer.println("/pong " + pingSender + " " + uuid);
                } else if(inputServer.startsWith("/pong")) {
                    endPingTime = System.currentTimeMillis();
                    long roundTripTime = endPingTime - client.getStartPingTime();
                    System.out.println(inputServer + " ( rtt = " + roundTripTime + " ms )");
                }else {
                    System.out.println(inputServer);
                }
            }
        } catch (IOException e) {
            System.out.println("Server connection was closed");
            System.exit(0);
        }
    }
}