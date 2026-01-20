package server;

import common.Protocol;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String nick;
    private volatile boolean kicked;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (socket) {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Servidor: Bem-vindo! Envie " + Protocol.NICK + "seu_nome");
            String line;

            // registrar nick
            while ((line = in.readLine()) != null) {
                if (line.startsWith(Protocol.NICK)) {
                    String desired = line.substring(Protocol.NICK.length()).trim();
                    if (desired.isEmpty()) { out.println("Servidor: nick invalido."); continue; }
                    if (server.isBanned(desired)) { out.println("Servidor: voce esta banido."); continue; }

                    if (server.registerNick(desired, this)) {
                        nick = desired;
                        out.println("Servidor: OK! Sala atual = lobby. Use /join sala | /who | /pm nick msg | /quit");
                        server.broadcastToRoom("lobby", "Servidor", nick + " entrou no chat.");
                        break;
                    } else {
                        out.println("Servidor: nick em uso. Tente outro.");
                    }
                } else {
                    out.println("Servidor: primeiro defina nick com " + Protocol.NICK + "seu_nome");
                }
            }

            // loop principal
            while ((line = in.readLine()) != null) {
                if (line.equalsIgnoreCase(Protocol.QUIT)) break;

                if (line.startsWith(Protocol.MSG)) {
                    String msg = line.substring(Protocol.MSG.length()).trim();
                    if (msg.startsWith("/kick ")) {
                        handleKick(msg.substring(6).trim());
                    } else if (msg.startsWith("/ban ")) {
                        handleBan(msg.substring(5).trim());
                    } else {
                        server.broadcastFromUser(nick, msg);
                    }
                } else if (line.startsWith(Protocol.JOIN)) {
                    String room = line.substring(Protocol.JOIN.length()).trim();
                    server.joinRoom(nick, room);
                } else if (line.equalsIgnoreCase(Protocol.WHO)) {
                    server.sendWho(nick);
                } else if (line.startsWith(Protocol.PM)) {
                    String rest = line.substring(Protocol.PM.length());
                    int idx = rest.indexOf(':');
                    if (idx > 0) {
                        String to = rest.substring(0, idx).trim();
                        String msg = rest.substring(idx + 1);
                        server.sendPrivate(nick, to, msg);
                    } else {
                        out.println("Servidor: formato PM invalido. Use /pm nick mensagem");
                    }
                } else {
                    out.println("Servidor: comando desconhecido.");
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (nick != null) {
                String room = server.getRoomOf(nick);
                server.removeClient(nick);
                if (!kicked) {
                    server.broadcastToRoom(room, "Servidor", nick + " saiu.");
                }
            }
        }
    }

    public void send(String message) {
        if (out != null) out.println(message);
    }

    public void disconnect(String reason) {
        kicked = true;
        if (out != null && reason != null && !reason.isBlank()) {
            out.println("Servidor: " + reason);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    private void handleKick(String target) {
        if (!server.isAdmin(nick)) {
            out.println("Servidor: comando admin. Sem permissao.");
            return;
        }
        server.kickNick(nick, target);
    }

    private void handleBan(String target) {
        if (!server.isAdmin(nick)) {
            out.println("Servidor: comando admin. Sem permissao.");
            return;
        }
        server.banNick(nick, target);
    }
}
