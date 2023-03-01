package es.iespuertodelacruz.cns.server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientThread extends Thread {

    private String user;
    private String nick;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Server server;
    private boolean isPingOn;

    public ClientThread(Socket socket, Server server) {

        try {
            this.server = server;
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            isPingOn = true;
        } catch (IOException e) {
            System.out.println("Cliente desconectado");
        }

    }

    @Override
    public void run() {
        try {
            doLogin();
            stayInChat();
            server.leaveChat(this);
            socket.close();
        } catch (IOException e) {
            System.out.println("Conexión cerrada");
        }
    }

    private void stayInChat() throws IOException {
        writer.println("*** ¡Bienvenido a la sala!\n*** Escribe /help para ver la lista de comandos.");
        String message;
        while ((message = reader.readLine()) != null && !socket.isClosed()) {

            if(message.startsWith("/ping")) {
                String[] parametros = message.split(" ");
                String uuid = parametros[2];
                server.executeCommand(this, message);
            } else if(message.startsWith("/")) {
                server.executeCommand(this, message);
            } else {
                server.sendPublicMessage("> " + nick + ": " + message);
            }

        }
    }

    private void doLogin() throws IOException {
        String user = "", pass = "";

        String message;
        boolean isLogged = false;
        sendMessageToClient("*** Inicia sesión con '/login' para poder continuar");
        while (!isLogged) {
            message = reader.readLine();
            if (message.startsWith("/login")) {
                String[] parametros = message.split(" ");
                if (parametros.length != 3) {
                    sendMessageToClient("*** Login incorrecto");
                } else {
                    user = parametros[1];
                    pass = parametros[2];
                    isLogged = server.login(user, pass);
                }
            } else {
                sendMessageToClient("*** Inicia sesión para poder continuar");
            }
        }

        server.saveLog(getDate() + " " + user + " conectado desde " + getIp());
        this.nick = user;
        this.user = user;

        server.addConnectedClient(nick);
    }

    public void sendMessageToClient(String message) {
        writer.println(message);
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public void closeSocket() {
        try {
            writer.println();
            socket.close();
        } catch (IOException e) {
            System.out.println("500 Error Interno Servidor");
        }
    }
    public String getUser() {
        return user;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIp() {
        return socket.getInetAddress().toString().replace("/", "");
    }

    public boolean isPingOn() {
        return isPingOn;
    }

    public void setPingOn(boolean pingOn) {
        isPingOn = pingOn;
    }
}