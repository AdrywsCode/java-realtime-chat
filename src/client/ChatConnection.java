package client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatConnection implements Closeable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;

    public void connect(String host, int port, Consumer<String> onMessage) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) onMessage.accept(line);
            } catch (IOException ignored) {}
        }, "chat-reader");
        readerThread.start();
    }

    public void sendLine(String line) {
        if (out != null) out.println(line);
    }

    @Override
    public void close() throws IOException {
        try { if (out != null) out.println("QUIT"); } catch (Exception ignored) {}
        if (socket != null) socket.close();
    }
}
