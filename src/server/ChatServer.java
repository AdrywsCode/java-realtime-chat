// src/server/ChatServer.java
package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import server.ClientHandler;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private final int port;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) { this.port = port; }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Servidor rodando na porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        }
    }

    public boolean registerNick(String nick, ClientHandler handler) {
        return clients.putIfAbsent(nick, handler) == null;
    }

    public void removeClient(String nick) {
        if (nick != null) clients.remove(nick);
    }

    public void broadcast(String from, String message) {
        String line = from + ": " + message;
        clients.values().forEach(c -> c.send(line));
    }

    public void sendPrivate(String from, String to, String message) {
        ClientHandler target = clients.get(to);
        if (target != null) target.send("[PM] " + from + ": " + message);
    }

    public static void main(String[] args) throws IOException {
        new ChatServer(5000).start();
    }
}

