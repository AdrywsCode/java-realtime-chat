// src/client/ChatClient.java
package client;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ChatClient {
    private static final Logger logger = Logger.getLogger(ChatClient.class.getName());

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Thread de leitura do servidor
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        logger.info(line);
                    }
                } catch (IOException _) {
                    // Connection closed
                }
            });
            reader.start();

            // Envio do usuário
            logger.info("Digite seu nick: ");
            String nick = userIn.readLine();
            serverOut.println("NICK:" + nick);

            String msg;
            while ((msg = userIn.readLine()) != null) {
                if (msg.equalsIgnoreCase("/quit")) {
                    serverOut.println("QUIT");
                    break;
                }
                // se o usuário digitar "/pm ana oi"
                if (msg.startsWith("/pm ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length == 3) serverOut.println("PM:" + parts[1] + ":" + parts[2]);
                    else logger.info("Use: /pm nick mensagem");
                } else {
                    serverOut.println("MSG:" + msg);
                }
            }
        }
    }
}
