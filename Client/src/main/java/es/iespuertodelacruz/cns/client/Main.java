package es.iespuertodelacruz.cns.client;


public class Main {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 6667;

    public static void main(String[] args) {

        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        if(args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        Client client = new Client(host, port);
        client.start();
    }
}
