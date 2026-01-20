package client.gui;

import client.ChatConnection;
import common.Protocol;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatWindow extends JFrame {
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Enviar");

    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("5000");
    private final JTextField nickField = new JTextField();
    private final JButton connectButton = new JButton("Conectar");
    private final JLabel statusLabel = new JLabel("Desconectado");

    private ChatConnection connection;
    private boolean connected;

    public ChatWindow() {
        super("Chat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 420));
        setLayout(new BorderLayout(8, 8));

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new BorderLayout(8, 8));
        JPanel leftFields = new JPanel(new BorderLayout(8, 8));
        JPanel rightFields = new JPanel(new BorderLayout(8, 8));

        leftFields.add(labeled("Host", hostField), BorderLayout.WEST);
        leftFields.add(labeled("Porta", portField), BorderLayout.CENTER);
        rightFields.add(labeled("Nick", nickField), BorderLayout.CENTER);
        rightFields.add(connectButton, BorderLayout.EAST);

        fields.add(leftFields, BorderLayout.WEST);
        fields.add(rightFields, BorderLayout.CENTER);
        top.add(fields, BorderLayout.CENTER);
        top.add(statusLabel, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setInputEnabled(false);

        connectButton.addActionListener(e -> connect());
        sendButton.addActionListener(e -> sendCurrentInput());
        inputField.addActionListener(e -> sendCurrentInput());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                disconnect();
            }
        });
    }

    private JPanel labeled(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    private void connect() {
        if (connected) return;

        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String nick = nickField.getText().trim();

        if (host.isEmpty() || portText.isEmpty() || nick.isEmpty()) {
            appendLine("Servidor: preencha host, porta e nick.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            appendLine("Servidor: porta invalida.");
            return;
        }

        try {
            connection = new ChatConnection();
            connection.connect(host, port, this::appendLine);
            connection.sendLine(Protocol.NICK + nick);
            connected = true;
            statusLabel.setText("Conectado");
            setInputEnabled(true);
            inputField.requestFocusInWindow();
        } catch (IOException ex) {
            appendLine("Servidor: falha ao conectar.");
        }
    }

    private void disconnect() {
        if (!connected || connection == null) return;
        try {
            connection.close();
        } catch (IOException ignored) {
        } finally {
            connected = false;
            statusLabel.setText("Desconectado");
            setInputEnabled(false);
        }
    }

    private void sendCurrentInput() {
        if (!connected || connection == null) return;
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        if (msg.equalsIgnoreCase("/quit")) {
            connection.sendLine(Protocol.QUIT);
            disconnect();
            return;
        }

        if (msg.startsWith("/join ")) {
            connection.sendLine(Protocol.JOIN + msg.substring(6).trim());
        } else if (msg.equalsIgnoreCase("/who")) {
            connection.sendLine(Protocol.WHO);
        } else if (msg.startsWith("/pm ")) {
            String[] parts = msg.split(" ", 3);
            if (parts.length == 3) {
                connection.sendLine(Protocol.PM + parts[1] + ":" + parts[2]);
            } else {
                appendLine("Servidor: use /pm nick mensagem");
            }
        } else {
            connection.sendLine(Protocol.MSG + msg);
        }

        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    private void appendLine(String line) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(line + System.lineSeparator());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatWindow window = new ChatWindow();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
