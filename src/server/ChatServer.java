package server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final int port;

    // nick -> handler
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    // nick -> room
    private final ConcurrentHashMap<String, String> userRoom = new ConcurrentHashMap<>();

    // banned nicks (lowercase)
    private final Set<String> banned = ConcurrentHashMap.newKeySet();

    private volatile String adminNick;
    private PrintWriter logWriter;
    private final Object logLock = new Object();
    private String currentLogDate;

    public ChatServer(int port) {
        this.port = port;
        this.currentLogDate = "";
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Servidor rodando na porta " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                log("Nova conexao de " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                new Thread(handler).start();
            }
        }
    }

    public boolean registerNick(String nick, ClientHandler handler) {
        if (isBanned(nick)) return false;
        boolean ok = clients.putIfAbsent(nick, handler) == null;
        if (ok) {
            userRoom.put(nick, "lobby");
            if (adminNick == null) {
                adminNick = nick;
                log("Admin definido: " + adminNick);
            }
        }
        return ok;
    }

    public void removeClient(String nick) {
        if (nick != null) {
            clients.remove(nick);
            userRoom.remove(nick);
            log("Cliente saiu: " + nick);
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
        log("MSG [" + room + "] " + from + ": " + message);
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
        log("PM " + from + " -> " + to + ": " + message);

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

    public boolean isAdmin(String nick) {
        return nick != null && nick.equals(adminNick);
    }

    public boolean isBanned(String nick) {
        if (nick == null) return false;
        return banned.contains(nick.toLowerCase(Locale.ROOT));
    }

    public void kickNick(String admin, String target) {
        ClientHandler handler = clients.get(target);
        if (handler == null) {
            ClientHandler sender = clients.get(admin);
            if (sender != null) sender.send("Servidor: usuario '" + target + "' nao encontrado.");
            return;
        }

        handler.disconnect("voce foi removido por um admin.");
        removeClient(target);
        log("KICK " + admin + " -> " + target);
    }

    public void banNick(String admin, String target) {
        if (target == null || target.isBlank()) return;
        banned.add(target.toLowerCase(Locale.ROOT));
        kickNick(admin, target);
        log("BAN " + admin + " -> " + target);
    }

    public void log(String message) {
        LocalDateTime now = LocalDateTime.now();
        String ts = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = "[" + ts + "] " + message;
        System.out.println(line);
        synchronized (logLock) {
            String logDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!logDate.equals(currentLogDate) || logWriter == null) {
                if (logWriter != null) logWriter.close();
                try {
                    logWriter = new PrintWriter(new FileWriter("server-" + logDate + ".log", true), true);
                    currentLogDate = logDate;
                } catch (IOException e) {
                    logWriter = null;
                    currentLogDate = "";
                }
            }
            if (logWriter != null) {
                logWriter.println(line);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer(5000).start();
    }
}
