package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final int port;

    // nick -> handler
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // nick -> room
    private final ConcurrentHashMap<String, String> userRoom = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor rodando na porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        }
    }

    public boolean registerNick(String nick, ClientHandler handler) {
        boolean ok = clients.putIfAbsent(nick, handler) == null;
        if (ok) userRoom.put(nick, "lobby");
        return ok;
    }

    public void removeClient(String nick) {
        if (nick != null) {
            clients.remove(nick);
            userRoom.remove(nick);
        }
    }

    public String getRoomOf(String nick) {
        return userRoom.getOrDefault(nick, "lobby");
    }

    public void joinRoom(String nick, String newRoom) {
        if (newRoom == null || newRoom.isBlank()) newRoom = "lobby";
        newRoom = sanitizeRoom(newRoom);

        String oldRoom = getRoomOf(nick);
        if (oldRoom.equals(newRoom)) return;

        userRoom.put(nick, newRoom);

        // avisos
        broadcastToRoom(oldRoom, "Servidor", nick + " saiu da sala " + oldRoom + ".");
        broadcastToRoom(newRoom, "Servidor", nick + " entrou na sala " + newRoom + ".");

        // confirma pro próprio usuário
        ClientHandler self = clients.get(nick);
        if (self != null) self.send("Servidor: agora voce esta na sala " + newRoom);
    }

    private String sanitizeRoom(String room) {
        // mantém simples: letras, números, _ e -
        room = room.trim();
        room = room.replaceAll("[^a-zA-Z0-9_-]", "");
        if (room.isEmpty()) return "lobby";
        return room;
    }

    public void broadcastFromUser(String from, String message) {
        String room = getRoomOf(from);
        broadcastToRoom(room, from, message);
    }

    public void broadcastToRoom(String room, String from, String message) {
        String line = "[" + room + "] " + from + ": " + message;

        clients.forEach((nick, handler) -> {
            if (getRoomOf(nick).equals(room)) {
                handler.send(line);
            }
        });
    }

    public boolean sendPrivate(String from, String to, String message) {
        ClientHandler target = clients.get(to);
        ClientHandler sender = clients.get(from);

        if (target == null) {
            if (sender != null) sender.send("Servidor: usuario '" + to + "' nao encontrado.");
            return false;
        }

        target.send("[PM] " + from + ": " + message);
        if (sender != null) sender.send("[PM -> " + to + "] " + message);

        return true;
    }

    public void sendWho(String requesterNick) {
        String room = getRoomOf(requesterNick);
        List<String> names = new ArrayList<>();

        clients.keySet().forEach(n -> {
            if (getRoomOf(n).equals(room)) names.add(n);
        });

        Collections.sort(names);

        ClientHandler req = clients.get(requesterNick);
        if (req != null) {
            req.send("Servidor: usuarios na sala '" + room + "': " + String.join(", ", names));
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer(5000).start();
    }
}
