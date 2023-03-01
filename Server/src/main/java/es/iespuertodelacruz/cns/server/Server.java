package es.iespuertodelacruz.cns.server;
import es.iespuertodelacruz.cns.server.database.UsersBD;
import es.iespuertodelacruz.cns.server.utils.AnsiEscapeCode;
import es.iespuertodelacruz.cns.server.utils.LogWriter;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
   private final int PORT;

    private LogWriter logWriter;
    private ArrayList<ClientThread> clientThreads;
    private Set<String> connectedClients;

    public Server(int port) {
        this.PORT = port;
        clientThreads = new ArrayList<>();
        connectedClients = new HashSet<>();
    }

    public void start() {

        System.out.println("Server initialized at port: " + PORT);
        logWriter = new LogWriter();

        while (true) {
            try(ServerSocket serverSocket = new ServerSocket(PORT)) {
                Socket       socket       = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket, this);
                clientThreads.add(clientThread);
                clientThread.start();

            } catch (IOException e) {
                System.err.println("500 Server Internal Error: ServerSocket closed");
            }
        }
    }

    public void executeCommand(ClientThread receiver, String message) {

        if (message.startsWith("/")) {
            String[] params = message.split(" ");
            String command = params[0];

            switch (command) {
                case "/login":
                    receiver.sendMessageToClient("*** You are already logged");
                    break;
                case "/nick":
                    if (params.length == 2) {
                        changeNick(receiver, params[1]);
                    }
                    break;
                case "/msg":
                    if (params.length >= 2) {
                        makePrivateMessage(receiver, params);
                    }
                    break;
                case "/userlist":
                    showConnectedUsers(receiver);
                    break;
                case "/ping":
                    if(params.length > 1) {
                        if(params[1].equals("on")) {
                            receiver.setPingOn(true);
                            receiver.sendMessageToClient("*** Ping enabled");
                            break;
                        }
                        if(params[1].equals("off")) {
                            receiver.setPingOn(false);
                            receiver.sendMessageToClient("*** Ping disabled");
                            break;
                        }
                        String pingReceiver = params[1];
                        String uuid = params[2];
                        doPing(receiver, pingReceiver, uuid);
                    }
                    break;
                case "/pong":
                    String pingSender = params[1];
                    String uuidPong = params[2];
                    doPong(receiver, pingSender, uuidPong);
                    break;
                case "/cls":
                    clearConsole(receiver);
                    break;
                case "/quit":
                    leaveChat(receiver);
                    break;
                case "/help":
                    showCommands(receiver);
                    break;
                default:
                    receiver.sendMessageToClient("*** Unknown command: " + command);
                    break;
            }
        }
    }

    private void makePrivateMessage(ClientThread receiver, String[] params) {
        String strReceiver = params[1];
        String strMessage = Arrays.stream(params, 2, params.length)
                .collect(Collectors.joining(" "));
        sendPrivateMessage(receiver, strReceiver, strMessage);
    }

    public synchronized void sendPublicMessage(String message) {
        for (ClientThread clientThread : clientThreads) {
            if(clientThread.getNick() != null) {
                clientThread.sendMessageToClient(message);
            }
        }
    }

    public synchronized void sendPrivateMessage(ClientThread sender, String receiver, String message) {
        ClientThread receiverClient = getClientThread(receiver);
        if (receiverClient != null) {

            if(!sender.getNick().equals(receiver)) {
                receiverClient.sendMessageToClient(AnsiEscapeCode.COLOR_VIOLET + "> "
                        + sender.getNick()
                        + "(private): "
                        + message
                        + AnsiEscapeCode.COLOR_RESET
                );
            }
            sender.sendMessageToClient(AnsiEscapeCode.COLOR_VIOLET + "> "
                    + "I"
                    + "(private): "
                    + message
                    + AnsiEscapeCode.COLOR_RESET
            );
            saveLog(getDate() + " msg origin(" + sender.getUser() + ", " + sender.getNick()
                    + ") receiver " + "(" + receiverClient.getUser() + ", "
                    + receiverClient.getNick() + ") " + message
            );
        } else {
            sender.sendMessageToClient("*** User " + receiver + " disconnected");
        }
    }

    public synchronized void showCommands(ClientThread sender) {
        sender.sendMessageToClient(
                "\n  /login <username> <password>\n"
                + "    Login\n\n"
                + "  /nick <new_nick> \n"
                + "    Change your nick in this session\n\n"
                + "  /msg <receiver> <message>\n"
                + "    Send a private message\n\n"
                + "  /userlist\n"
                + "    Show users connected\n\n"
                + "  /ping <on|off|nick>\n"
                + "    'on' Enable ping. It's enable by Default\n"
                + "    'off' Disable ping\n"
                + "    'nick' Ping the user if they is connected\n\n"
                + "  /cls\n"
                + "    Clean the console\n\n"
                + "  /quit \n"
                + "    Leave the chat\n\n"
                + "  /help \n"
                + "    Show all commands available\n\n"
        );
    }

    private void clearConsole(ClientThread sender)  {
        sender.sendMessageToClient(AnsiEscapeCode.CLEAR_CONSOLE);
    }

    public void leaveChat(ClientThread sender) {
        connectedClients.remove(sender.getNick());
        sendPublicMessage("*** " + sender.getNick() + " has left the chat :(");
        saveLog(getDate() + " (" + sender.getNick() + ", "
                + sender.getUser() + ")" + " disconnected from "
                + sender.getIp()
        );
        clientThreads.remove(sender);
        sender.closeSocket();
    }

    public synchronized boolean login(String username, String password) {
        return UsersBD.checkPassword(username, password);
    }

    public synchronized ClientThread getClientThread(String nick) {
        for (ClientThread sender : clientThreads) {
            if (sender.getNick().equals(nick)) {
                return sender;
            }
        }
        return null;
    }

    public synchronized void addConnectedClient(String nick) {
        connectedClients.add(nick);
    }

    private void removeConnectedClient(String nick) {
        connectedClients.remove(nick);
    }


    public synchronized void showConnectedUsers(ClientThread emisor) {
        StringBuilder userListBuilder = new StringBuilder();
        for (String nick : connectedClients) {
            userListBuilder.append(nick).append("  ");
        }

        emisor.sendMessageToClient("*** Connected ("
                + connectedClients.size()
                + ")  "
                + userListBuilder
        );
    }

    public synchronized void changeNick(ClientThread sender, String nick) {

        if (connectedClients.contains(nick)) {
            sender.sendMessageToClient("*** Nickname already in use");
            return;
        }

        Map<String, String> users = UsersBD.getBdUsers();
        if(users.containsKey(nick)) {
            sender.sendMessageToClient("*** Nick already in use");
            return;
        }

        String previousName = sender.getNick();
        removeConnectedClient(previousName);
        sender.setNick(nick);
        addConnectedClient(nick);

        sendPublicMessage("*** {" + previousName + "} now is known as {" + nick + "}");
    }

    public synchronized void doPing(ClientThread sender, String pingReceiver, String uuid) {
        ClientThread receptorPingThread = getClientThread(pingReceiver);

        for(ClientThread clientThread : clientThreads) {
            boolean isNickEqualsToUser = clientThread.getUser().equals(pingReceiver);
            boolean isNickNotEqualsToUsuario = !clientThread.getNick().equals(pingReceiver);

            if(isNickEqualsToUser && isNickNotEqualsToUsuario) {
                sender.sendMessageToClient("*** " + pingReceiver + " has changed his nickname to "
                        + clientThread.getNick()
                );
                return;
            }
        }

        if(receptorPingThread == null) {
            sender.sendMessageToClient("*** " + pingReceiver + " is not connected");
            return;
        }

        if(!receptorPingThread.isPingOn()) {
            sender.sendMessageToClient("*** " + pingReceiver + " has ping disabled");
            return;
        }


        receptorPingThread.sendMessageToClient("/ping " + sender.getNick() + " " + uuid);
    }

    public synchronized void doPong(ClientThread pongSender, String pingSender, String uuidPong) {
        ClientThread senderPingThread = getClientThread(pingSender);
        senderPingThread.sendMessageToClient("/pong " + pongSender.getNick() + " " + uuidPong);
    }

    public synchronized void saveLog(String log) {
        logWriter.write(log);
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

}
