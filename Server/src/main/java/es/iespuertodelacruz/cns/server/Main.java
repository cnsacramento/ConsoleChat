package es.iespuertodelacruz.cns.server;

public class Main {
    private static final int DEFAULT_PORT = 6667;
    public static void main(String[] args) {

        final int PORT = (args.length == 1) ?
                Integer.parseInt(args[0]) :
                DEFAULT_PORT;

        Server server = new Server(PORT);
        server.start();
    }
}