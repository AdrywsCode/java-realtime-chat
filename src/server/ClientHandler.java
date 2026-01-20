// src/server/ClientHandler.java
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String nick;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("NICK:")) {
                    String desired = line.substring("NICK:".length()).trim();
                    if (desired.isEmpty()) {
                        out.println("ERRO:Nick vazio");
                        continue;
                    }
                    if (server.registerNick(desired, this)) {
                        nick = desired;
                        out.println("OK:NICK");
                    } else {
                        out.println("ERRO:Nick em uso");
                    }
                    continue;
                }

                if (line.equals("QUIT")) {
                    break;
                }

                if (nick == null) {
                    out.println("ERRO:Defina NICK primeiro");
                    continue;
                }

                if (line.startsWith("PM:")) {
                    String[] parts = line.split(":", 3);
                    if (parts.length == 3) {
                        server.sendPrivate(nick, parts[1], parts[2]);
                    } else {
                        out.println("ERRO:PM invalido");
                    }
                    continue;
                }

                if (line.startsWith("MSG:")) {
                    String msg = line.substring("MSG:".length());
                    server.broadcast(nick, msg);
                    continue;
                }
            }
        } catch (IOException e) {
            logger.info("Conexao encerrada: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void cleanup() {
        server.removeClient(nick);
        try {
            socket.close();
        } catch (IOException _) {
            // Ignore
        }
    }
}
