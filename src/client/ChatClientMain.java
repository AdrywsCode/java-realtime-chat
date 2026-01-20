package client;

import common.Protocol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatClientMain {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;

        ChatConnection conn = new ChatConnection();
        conn.connect(host, port, System.out::println);

        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Nick: ");
        String nick = userIn.readLine();
        conn.sendLine(Protocol.NICK + nick);

        String msg;
        while ((msg = userIn.readLine()) != null) {
            if (msg.equalsIgnoreCase("/quit")) { conn.sendLine(Protocol.QUIT); break; }

            if (msg.startsWith("/join ")) {
                conn.sendLine(Protocol.JOIN + msg.substring(6).trim());
                continue;
            }
            if (msg.equalsIgnoreCase("/who")) {
                conn.sendLine(Protocol.WHO);
                continue;
            }
            if (msg.startsWith("/pm ")) {
                String[] parts = msg.split(" ", 3);
                if (parts.length == 3) conn.sendLine(Protocol.PM + parts[1] + ":" + parts[2]);
                else System.out.println("Use: /pm nick mensagem");
                continue;
            }

            conn.sendLine(Protocol.MSG + msg);
        }

        conn.close();
    }
}
