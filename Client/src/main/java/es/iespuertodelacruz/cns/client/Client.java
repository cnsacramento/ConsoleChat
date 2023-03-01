package es.iespuertodelacruz.cns.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

public class Client {
    private final String HOST;
    private final int PORT;
    private long startPingTime;

    public Client(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public void start() {

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader readerSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader readerConsole = new BufferedReader(new InputStreamReader(System.in))) {

            Thread readThread = new Thread(new ReaderThread(readerSocket, writer, this));
            readThread.start();

            String inputClient;
            do {
                inputClient = readerConsole.readLine();

                if(inputClient.equals("/quit")) {
                    break;
                }

                if(inputClient.startsWith("/ping")) {
                    startPingTime = System.currentTimeMillis();
                    writer.println(inputClient + " " + UUID.randomUUID());
                } else {
                    writer.println(inputClient);
                }

            } while (!socket.isClosed());

            readThread.interrupt();

            System.exit(0);
        } catch (UnknownHostException ex) {
            System.err.println("404 Server not found");
        } catch (IOException ex) {
            System.err.println("The connection was closed");
        }
    }

    public long getStartPingTime() {
        return startPingTime;
    }

}


