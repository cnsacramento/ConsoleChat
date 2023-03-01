package es.iespuertodelacruz.cns.server.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class LogWriter {
    private static final String PATH = "/tmp/chat.logs";

    public synchronized void write(String log) {

        try (FileWriter fileWriter = new FileWriter(PATH, true);
             BufferedWriter writer = new BufferedWriter(fileWriter);
        ) {
            writer.write(log + "\n");
        } catch (Exception ex) {
            System.err.println("Could not write log");
        }
    }

}
